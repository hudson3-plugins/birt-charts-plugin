/*******************************************************************************
 *
 * Copyright (c) 2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *  Winston Prakash
 *
 *******************************************************************************/

package org.eclipse.hudson.plugins.birtcharts;

import hudson.Extension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.eclipse.birt.chart.api.ChartEngine;
import org.eclipse.birt.chart.device.EmptyUpdateNotifier;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.device.IDisplayServer;
import org.eclipse.birt.chart.device.IImageMapEmitter;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.IGenerator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.core.framework.PlatformConfig;
import org.eclipse.hudson.graph.DataSet;
import org.eclipse.hudson.graph.GraphSupport;
import org.eclipse.hudson.graph.GraphSupportDescriptor;
import org.eclipse.hudson.graph.MultiStageTimeSeries;

/**
 * BIRT Chart implementation for Hudson Graph Support
 * @author Winston Prakash
 */
public class BirtChartSupport extends GraphSupport {

    private String chartTitle;
    private String xAxisLabel;
    private String yAxisLabel;
    private IDeviceRenderer dRenderer = null;
    private int chartType = 3; // 1 - Stacked Area, 2 - Line, 3 - Stacked Bar
    private DataSet dataSet;

    public BirtChartSupport() {
    }

    @Override
    public void setChartType(int chartType) {
        this.chartType = chartType;
    }

    @Override
    public void setTitle(String title) {
        chartTitle = title;
    }

    @Override
    public void setXAxisLabel(String xLabel) {
        xAxisLabel = xLabel;
    }

    @Override
    public void setYAxisLabel(String yLabel) {
        yAxisLabel = yLabel;
    }

    @Override
    public BufferedImage render(int width, int height) {
        Chart cm = createBirtChart();
        // obtain a png image device renderer
        ChartEngine ce = null;
        try {
            PlatformConfig config = new PlatformConfig();
            config.setProperty("STANDALONE", "true");
            ce = ChartEngine.instance(config);
            dRenderer = ce.getRenderer("dv.PNG");
        } catch (ChartException ex) {
            ex.printStackTrace();
        }

        BufferedImage image = new BufferedImage(width,
                height,
                BufferedImage.TYPE_INT_RGB);
        //Draw the chart in the buffered image
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        dRenderer.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, g2d);
        dRenderer.setProperty(IDeviceRenderer.CACHED_IMAGE, image);

        try {

            Bounds bo = BoundsImpl.create(0, 0, width, height);
            // convert pixels to points (1 inch = 72 points = dpi pixels )
            // 1 pixel = 72/dpi points
            bo.scale(72d / dRenderer.getDisplayServer().getDpiResolution());
            IGenerator gr = ce.getGenerator();

            IDisplayServer dServer = dRenderer.getDisplayServer();
            GeneratedChartState gcs =
                    gr.build(dServer,
                    cm,
                    bo,
                    null,
                    null,
                    null);
            dRenderer.setProperty(IDeviceRenderer.UPDATE_NOTIFIER,
                    new EmptyUpdateNotifier(cm, gcs.getChartModel()));
            gr.render(dRenderer, gcs); // Style processor

        } catch (ChartException ex) {
            ex.printStackTrace();
        }

        return image;
    }

    private Chart createBirtChart() {
        
        return new BirtChart(chartTitle, xAxisLabel, yAxisLabel, dataSet).createChart();
        
    }

    @Override
    public void setData(DataSet data) {
        dataSet = data;
    }

    @Override
    public String getImageMap(String id, int width, int height) {
        //Unfortunately we have to render it again, because the map is loaded lazily in another HTTP request
        render(width, height);
        String imageMap = ((IImageMapEmitter) dRenderer).getImageMap();
        StringBuilder sb = new StringBuilder();
        sb.append("<map id=\"map\" name=\"" + id + "\"'>");
        sb.append(imageMap);
        sb.append("</map>");
        return sb.toString();
    }

    @Override
    public void setMultiStageTimeSeries(List<MultiStageTimeSeries> multiStageTimeSeries) {
    }

    @Extension
    public static class DescriptorImpl extends GraphSupportDescriptor {

        @Override
        public String getDisplayName() {
            return "Birt Charts";
        }
    }
}
