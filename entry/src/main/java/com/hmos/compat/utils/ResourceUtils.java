/*
 * Copyright (C) 2020-21 Application Library Engineering Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hmos.compat.utils;

import ohos.agp.components.element.Element;
import ohos.agp.components.element.PixelMapElement;
import ohos.app.Context;
import ohos.global.resource.Resource;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Size;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resource Util class.
 */
public class ResourceUtils {
    private static final HiLogLabel HILOG_LABEL = new HiLogLabel(0, 0, "ResourceUtils");

    private ResourceUtils() {
    }

    /**
     * Function to get element from resource.
     *
     * @param context context
     * @param resourceId resourceId
     * @return drawable
     */
    public static Element getDrawable(Context context, int resourceId) {
        Element drawable = null;
        if (resourceId != 0) {
            try {
                Resource resource = context.getResourceManager().getResource(resourceId);
                drawable = prepareElement(resource);
            } catch (Exception e) {
                Logger.getGlobal().log(Level.INFO, "Exception");
            }
        }
        return drawable;
    }

    /**
     * Function to get PixelMapElement.
     *
     * @param resource resource
     * @return PixelMapElement
     */
    public static PixelMapElement prepareElement(Resource resource) throws IOException {
        return new PixelMapElement(preparePixelMap(resource));
    }

    /**
     * Function to get PixelMap.
     *
     * @param resource resource
     * @return imageSource
     */
    public static PixelMap preparePixelMap(Resource resource) throws IOException {
        ImageSource.SourceOptions srcOpts = new ImageSource.SourceOptions();
        ImageSource imageSource = null;
        try {
            imageSource = ImageSource.create(readResource(resource), srcOpts);
        } catch (Exception exception) {
            HiLog.error(HILOG_LABEL, "exception in preparePixelMap");
        } finally {
            close(resource);
        }
        if (imageSource == null) {
            throw new FileNotFoundException();
        }
        ImageSource.DecodingOptions decodingOpts = new ImageSource.DecodingOptions();
        decodingOpts.desiredSize = new Size(0, 0);
        decodingOpts.desiredRegion = new ohos.media.image.common.Rect(0, 0, 0, 0);
        decodingOpts.desiredPixelFormat = PixelFormat.ARGB_8888;
        return imageSource.createPixelmap(decodingOpts);
    }

    private static byte[] readResource(Resource resource) {
        final int bufferSize = 1024;
        final int ioEnd = -1;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[bufferSize];
        while (true) {
            try {
                int readLen = resource.read(buffer, 0, bufferSize);
                if (readLen == ioEnd) {
                    break;
                }
                output.write(buffer, 0, readLen);
            } catch (Exception exception) {
                HiLog.error(HILOG_LABEL, "exception in readResource");
            }
        }
        return output.toByteArray();
    }

    private static void close(Resource resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception exception) {
                HiLog.error(HILOG_LABEL, "exception in close");
            }
        }
    }
}