/*
 * Copyright (C) 2009-2010 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

// Taken from: http://github.com/ctrlaltdel/TahoeLAFS-android

package info.lamatricexiste.network.Network;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class DownloadFile {

    private static String TAG = "DownloadFile";
    // FIXME: Get the real application version without context ?
    private final String USERAGENT = "Android/" + android.os.Build.VERSION.RELEASE + " ("
            + android.os.Build.MODEL + ") NetworkDiscovery/0.3.3";
    private HttpClient httpclient;

    public DownloadFile(String url, FileOutputStream out) throws IOException {
        Log.i(TAG, "Downloading " + url);
        httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter("http.useragent", USERAGENT);
        InputStream in = openURL(url);
        final ReadableByteChannel inputChannel = Channels.newChannel(in);
        final WritableByteChannel outputChannel = Channels.newChannel(out);

        try {
            fastChannelCopy(inputChannel, outputChannel);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (inputChannel != null) {
                inputChannel.close();
            }
            if (outputChannel != null) {
                outputChannel.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    private InputStream openURL(String url) {
        HttpGet httpget = new HttpGet(url);
        HttpResponse response;
        try {
            try {
                response = httpclient.execute(httpget);
            } catch (SSLException e) {
                Log.i(TAG, "SSL Certificate is not trusted");
                response = httpclient.execute(httpget);
            }
            Log.i(TAG, "Status:[" + response.getStatusLine().toString() + "]");
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                return entity.getContent();
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, "There was a protocol based error", e);
        } catch (IOException e) {
            Log.e(TAG, "There was an IO Stream related error", e);
        }

        return null;
    }

    public static void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest)
            throws IOException, NullPointerException {
        if (src != null && dest != null) {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
            while (src.read(buffer) != -1) {
                // prepare the buffer to be drained
                buffer.flip();
                // write to the channel, may block
                dest.write(buffer);
                // If partial transfer, shift remainder down
                // If buffer is empty, same as doing clear()
                buffer.compact();
            }
            // EOF will leave buffer in fill state
            buffer.flip();
            // make sure the buffer is fully drained.
            while (buffer.hasRemaining()) {
                dest.write(buffer);
            }
        }
    }
}
