/**
 * Copyright 2017 Bartosz Schiller
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ahmetkocu.pdfrendereview;

import android.graphics.RectF;

import com.ahmetkocu.pdfrendereview.util.Constants;
import com.ahmetkocu.pdfrendereview.util.MathUtils;
import com.ahmetkocu.pdfrendereview.util.Util;
import com.shockwave.pdfium.util.SizeF;

import static com.ahmetkocu.pdfrendereview.util.Constants.PRELOAD_OFFSET;


class PagesLoader {

    private PDFView pdfView;
    private float xOffset;
    private float yOffset;
    private final RectF thumbnailRect = new RectF(0, 0, 1, 1);
    private final int preloadOffset;
    private final Holder firstHolder = new Holder();
    private final Holder lastHolder = new Holder();
    private final GridSize firstGrid = new GridSize();
    private final GridSize lastGrid = new GridSize();

    private class Holder {
        int page;
        int row;
        int col;
    }

    private class GridSize {
        int rows;
        int cols;
    }

    PagesLoader(PDFView pdfView) {
        this.pdfView = pdfView;
        this.preloadOffset = Util.getDP(pdfView.getContext(), PRELOAD_OFFSET);
    }

    private void getPageColsRows(GridSize grid, int pageIndex) {
        SizeF size = pdfView.pdfFile.getPageSize(pageIndex);
        float ratioX = 1f / size.getWidth();
        float ratioY = 1f / size.getHeight();
        final float partHeight = (Constants.PART_SIZE * ratioY) / pdfView.getZoom();
        final float partWidth = (Constants.PART_SIZE * ratioX) / pdfView.getZoom();
        grid.rows = MathUtils.ceil(1f / partHeight);
        grid.cols = MathUtils.ceil(1f / partWidth);
    }

    private Holder getPageAndCoordsByOffset(Holder holder, GridSize grid, float localXOffset,
                                            float localYOffset, boolean endOffset) {
        float fixedXOffset = -MathUtils.max(localXOffset, 0);
        float fixedYOffset = -MathUtils.max(localYOffset, 0);
        float offset = pdfView.isSwipeVertical() ? fixedYOffset : fixedXOffset;
        holder.page = pdfView.pdfFile.getPageAtOffset(offset, pdfView.getZoom());
        getPageColsRows(grid, holder.page);
        SizeF scaledPageSize = pdfView.pdfFile.getScaledPageSize(holder.page, pdfView.getZoom());
        float rowHeight = scaledPageSize.getHeight() / grid.rows;
        float colWidth = scaledPageSize.getWidth() / grid.cols;
        float row, col;
        float secondaryOffset = pdfView.pdfFile.getSecondaryPageOffset(holder.page, pdfView.getZoom());
        if (pdfView.isSwipeVertical()) {
            row = Math.abs(fixedYOffset - pdfView.pdfFile.getPageOffset(holder.page, pdfView.getZoom())) / rowHeight;
            col = MathUtils.min(fixedXOffset - secondaryOffset, 0) / colWidth;
        } else {
            col = Math.abs(fixedXOffset - pdfView.pdfFile.getPageOffset(holder.page, pdfView.getZoom())) / colWidth;
            row = MathUtils.min(fixedYOffset - secondaryOffset, 0) / rowHeight;
        }

        if (endOffset) {
            holder.row = MathUtils.ceil(row);
            holder.col = MathUtils.ceil(col);
        } else {
            holder.row = MathUtils.floor(row);
            holder.col = MathUtils.floor(col);
        }
        return holder;
    }

    private void loadVisible() {
        float scaledPreloadOffset = preloadOffset * pdfView.getZoom();
        float firstXOffset = -xOffset + scaledPreloadOffset;
        float lastXOffset = -xOffset - pdfView.getWidth() - scaledPreloadOffset;
        float firstYOffset = -yOffset + scaledPreloadOffset;
        float lastYOffset = -yOffset - pdfView.getHeight() - scaledPreloadOffset;

        getPageAndCoordsByOffset(firstHolder, firstGrid, firstXOffset, firstYOffset, false);
        getPageAndCoordsByOffset(lastHolder, lastGrid, lastXOffset, lastYOffset, true);

        for (int i = firstHolder.page; i <= lastHolder.page; i++) {
            loadPageImage(i);
        }
    }

    private void loadPageImage(int page) {
        SizeF pageSize = pdfView.pdfFile.getPageSize(page);
        float thumbnailWidth = pageSize.getWidth() * (Constants.THUMBNAIL_RATIO * pdfView.getZoom());
        float thumbnailHeight = pageSize.getHeight() * (Constants.THUMBNAIL_RATIO * pdfView.getZoom());
        if (!pdfView.cacheManager.containsThumbnail(page, thumbnailRect)) {
            pdfView.renderingHandler.addRenderingTask(page,
                    thumbnailWidth, thumbnailHeight, thumbnailRect,
                    true, 0, pdfView.isBestQuality(), pdfView.isAnnotationRendering());
        }
    }

    void loadPages() {
        xOffset = -MathUtils.max(pdfView.getCurrentXOffset(), 0);
        yOffset = -MathUtils.max(pdfView.getCurrentYOffset(), 0);

        loadVisible();
    }
}
