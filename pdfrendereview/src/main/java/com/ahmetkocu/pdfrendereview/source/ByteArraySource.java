/*
 * Copyright (C) 2016 Bartosz Schiller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ahmetkocu.pdfrendereview.source;

import android.content.Context;
import android.os.ParcelFileDescriptor;

import com.ahmetkocu.pdfrendereview.MyPdfRenderer;
import com.ahmetkocu.pdfrendereview.util.FileUtils;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;

public class ByteArraySource implements DocumentSource {

    private byte[] data;

    public ByteArraySource(byte[] data) {
        this.data = data;
    }

    @Override
    public PdfDocument createDocument(Context context, PdfiumCore core, String password) throws IOException {
        return core.newDocument(data, password);
    }

    @Override
    public MyPdfRenderer createDocument(Context context, String password) throws IOException {
        boolean canWrite = FileUtils.writeResponseBodyToDisk(context, data);
        if (!canWrite) {
            throw new IOException("Can not write pdf file to cache");
        }

        File documentFile = FileUtils.readCacheToByteArray(context, FileUtils.fileName);
        ParcelFileDescriptor mFileDescriptor = ParcelFileDescriptor.open(documentFile, ParcelFileDescriptor.MODE_READ_ONLY);

        if (mFileDescriptor == null) {
            throw new IOException("Can not create ParcelFileDescriptor");
        }

        return new MyPdfRenderer(mFileDescriptor);
    }
}
