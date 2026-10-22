package spam.blocker.qa;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public final class QaDocumentProvider extends ContentProvider {
    private static final String AUTHORITY = "spam.blocker.qa.test.documents";
    private static final String DATA = "data";
    private static final String PUT = "put";
    private static final String CLEAR = "clear";
    private static final ConcurrentHashMap<String, byte[]> DOCUMENTS = new ConcurrentHashMap<>();

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String name = name(uri);
        String[] columns = projection == null
                ? new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE}
                : projection;
        MatrixCursor cursor = new MatrixCursor(columns);
        Object[] row = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            if (OpenableColumns.DISPLAY_NAME.equals(columns[i])) row[i] = name;
            if (OpenableColumns.SIZE.equals(columns[i])) {
                byte[] content = DOCUMENTS.get(name);
                row[i] = content == null ? 0L : (long) content.length;
            }
        }
        cursor.addRow(row);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return name(uri).endsWith(".csv") ? "text/csv" : "application/octet-stream";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return DOCUMENTS.remove(name(uri)) == null ? 0 : 1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        String documentName = name(uri);
        boolean reading = mode.startsWith("r");
        byte[] content = reading ? DOCUMENTS.get(documentName) : null;
        if (reading && content == null) throw new FileNotFoundException(documentName);
        try {
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
            if (reading) {
                Thread writer = new Thread(() -> {
                    try (ParcelFileDescriptor.AutoCloseOutputStream output =
                                 new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])) {
                        output.write(content);
                    } catch (IOException ignored) {
                    }
                }, "qa-document-read");
                writer.setDaemon(true);
                writer.start();
                return pipe[0];
            }

            Thread reader = new Thread(() -> {
                ByteArrayOutputStream outputContent = new ByteArrayOutputStream();
                try (ParcelFileDescriptor.AutoCloseInputStream input =
                             new ParcelFileDescriptor.AutoCloseInputStream(pipe[0])) {
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = input.read(buffer)) != -1) outputContent.write(buffer, 0, count);
                    DOCUMENTS.put(documentName, outputContent.toByteArray());
                } catch (IOException ignored) {
                }
            }, "qa-document-write");
            reader.setDaemon(true);
            reader.start();
            return pipe[1];
        } catch (IOException exception) {
            throw new FileNotFoundException(exception.getMessage());
        }
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (arg == null || arg.isEmpty()) throw new IllegalArgumentException("Missing QA document name");
        String documentName = arg;
        if (PUT.equals(method)) {
            byte[] bytes = extras == null ? null : extras.getByteArray(DATA);
            if (bytes == null) throw new IllegalArgumentException("Missing QA document bytes");
            DOCUMENTS.put(documentName, bytes);
        }
        if (CLEAR.equals(method)) DOCUMENTS.remove(documentName);
        Bundle result = new Bundle();
        result.putByteArray(DATA, DOCUMENTS.get(documentName));
        return result;
    }

    private static String name(Uri uri) {
        String name = uri.getLastPathSegment();
        if (name == null || name.isEmpty() || name.contains("/")) {
            throw new IllegalArgumentException("Invalid QA document URI");
        }
        return name;
    }

    public static Uri uriFor(String name) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY).appendPath(name).build();
    }

    public static void putDocument(Context context, String name, byte[] bytes) {
        Bundle data = new Bundle();
        data.putByteArray(DATA, bytes);
        context.getContentResolver().call(uriFor(name), PUT, name, data);
    }

    public static byte[] getDocument(Context context, String name) {
        Bundle result = context.getContentResolver().call(uriFor(name), "get", name, null);
        return result == null ? null : result.getByteArray(DATA);
    }

    public static void clearDocument(Context context, String name) {
        context.getContentResolver().call(uriFor(name), CLEAR, name, null);
    }
}
