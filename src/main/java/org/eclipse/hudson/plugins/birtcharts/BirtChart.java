/*
 * The MIT License
 * 
 * Copyright (c) 2012, Oracle Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.eclipse.hudson.plugins.birtcharts;

import java.awt.Color;
import java.util.List;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.*;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.LineAttributesImpl;
import org.eclipse.birt.chart.model.attribute.impl.TooltipValueImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.Trigger;
import org.eclipse.birt.chart.model.data.impl.ActionImpl;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.TriggerImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.impl.AreaSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.LineSeriesImpl;
import org.eclipse.hudson.graph.DataSet;
import org.eclipse.hudson.graph.GraphSeries;

/**
 * Creates a BIRT Chart
 *
 * @author Winston Prakash
 */
class BirtChart {

    protected String chartTitle;
    protected String xAxisLabel;
    protected String yAxisLabel;
    protected DataSet dataSet;

    BirtChart(String title, String xLabel, String yLabel, DataSet data) {
        chartTitle = title;
        xAxisLabel = xLabel;
        yAxisLabel = yLabel;
        dataSet = data;
    }

    Chart createChart() {

        ChartWithAxes chartWithAxes = ChartWithAxesImpl.create();

        // Plot attributes

        chartWithAxes.getBlock().setBackground(ColorDefinitionImpl.CREAM().brighter());
        chartWithAxes.getBlock().getOutline().setVisible(true);

        // Title

        chartWithAxes.getTitle().getLabel().getCaption().setValue(chartTitle);


        // Legend
        Legend lg = chartWithAxes.getLegend();
        lg.setItemType(LegendItemType.SERIES_LITERAL);
        lg.getText().getFont().setSize(10);
        lg.getText().getFont().setBold(true);
        lg.getInsets().set(5, 0, 5, 5);
        lg.getClientArea().getInsets().set(5, 5, 5, 5);


        // X-Axis
        Axis xAxis = chartWithAxes.getPrimaryBaseAxes()[0];
        xAxis.setType(AxisType.TEXT_LITERAL);
        xAxis.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
        xAxis.getOrigin().setType(IntersectionType.MIN_LITERAL);
        xAxis.getTitle().getCaption().setValue(xAxisLabel);
        xAxis.getLabel().getCaption().getFont().setRotation(90.);
        xAxis.getLabel().getCaption().getFont().setItalic(true);
        xAxis.getLabel().getCaption().setColor(ColorDefinitionImpl.BLUE().darker());
        xAxis.getLineAttributes().setColor(ColorDefinitionImpl.BLUE().darker());
        xAxis.getMajorGrid().setLineAttributes(LineAttributesImpl.create(ColorDefinitionImpl.PINK().translucent(),
                LineStyle.SOLID_LITERAL,
                1));

        setXData(xAxis);

        // Y-Axis
        Axis yAxis = chartWithAxes.getPrimaryOrthogonalAxis(xAxis);
        yAxis.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
        yAxis.getTitle().getCaption().setValue(yAxisLabel);
        yAxis.getTitle().getCaption().setColor(ColorDefinitionImpl.BLUE().darker());
        yAxis.getLabel().getCaption().getFont().setItalic(true);
        yAxis.getLabel().getCaption().setColor(ColorDefinitionImpl.BLUE().darker());
        yAxis.getLineAttributes().setColor(ColorDefinitionImpl.BLUE().darker());
        yAxis.getTitle().setVisible(true);
        yAxis.getMajorGrid().setLineAttributes(LineAttributesImpl.create(ColorDefinitionImpl.PINK().translucent(),
                LineStyle.SOLID_LITERAL,
                1));
        // Y-Series

        setYData(yAxis);

        return chartWithAxes;
    }

    protected void setXData(Axis xAxis) {

        GraphSeries<String> grpahXSeries = dataSet.getXSeries();

        String[] seriesData = grpahXSeries.getData().toArray(new String[grpahXSeries.getData().size()]);

        TextDataSet xData = TextDataSetImpl.create(seriesData);

        Series xSeries = SeriesImpl.create();
        xSeries.setDataSet(xData);
        xSeries.setSeriesIdentifier(grpahXSeries.getCaption());

        SeriesDefinition xSeriesDefinition = SeriesDefinitionImpl.create();
        xAxis.getSeriesDefinitions().add(xSeriesDefinition);

        xSeriesDefinition.getSeries().add(xSeries);

    }

    protected void setYData(Axis yAxis) {

        List<GraphSeries<Number>> grpahYSerieses = dataSet.getYSeries();

        for (GraphSeries<Number> grpahYSeries : grpahYSerieses) {

            if (grpahYSeries.getData().size() > 0) {

                Number[] seriesData = grpahYSeries.getData().toArray(new Number[grpahYSeries.getData().size()]);

                NumberDataSet yData = NumberDataSetImpl.create(seriesData);

                Series ySeries = BarSeriesImpl.create();

                switch (grpahYSeries.getType()) {
                    case GraphSeries.TYPE_BAR:
                        ySeries = BarSeriesImpl.create();
                        ((BarSeries) ySeries).setRiser(RiserType.TUBE_LITERAL);
                        ySeries.setTranslucent(true);
                        break;
                    case GraphSeries.TYPE_AREA:
                        ySeries = AreaSeriesImpl.create();
                        ySeries.setTranslucent(true);
                        break;
                    case GraphSeries.TYPE_LINE:
                        ySeries = LineSeriesImpl.create();
                        LineSeries ls = (LineSeries) ySeries;
                        ls.getLineAttributes().setColor(getColor(grpahYSeries.getColor()));
                        for (int i = 0; i < ls.getMarkers().size(); i++) {
                            ((Marker) ls.getMarkers().get(i)).setType(MarkerType.CIRCLE_LITERAL);
                            ((Marker) ls.getMarkers().get(i)).setSize(2);
                        }
                }

                ySeries.setDataSet(yData);

                if (grpahYSeries.isStacked()) {
                    ySeries.setStacked(true);
                }

                ySeries.setSeriesIdentifier(grpahYSeries.getCaption());
                if (grpahYSeries.isValueLabelDisplayed()) {
                    ySeries.getLabel().setVisible(true);
                } else {
                    ySeries.getLabel().setVisible(false);
                }

                if (grpahYSeries.isLabelInside()) {
                    ySeries.setLabelPosition(Position.INSIDE_LITERAL);
                    ySeries.getLabel().getCaption().getFont().setRotation(90.);
                }

                SeriesDefinition ySeriesDefinition = SeriesDefinitionImpl.create();
                ySeriesDefinition.getSeriesPalette().update(getColor(grpahYSeries.getColor()));
                yAxis.getSeriesDefinitions().add(ySeriesDefinition);
                ySeriesDefinition.getSeries().add(ySeries);

                Trigger tr1 = TriggerImpl.create(TriggerCondition.ONMOUSEOVER_LITERAL,
                        ActionImpl.create(ActionType.SHOW_TOOLTIP_LITERAL,
                        TooltipValueImpl.create(200, "value")));
//              Trigger tr2 = TriggerImpl.create(TriggerCondition.ONCLICK_LITERAL,
//                                  ActionImpl.create(ActionType.URL_REDIRECT_LITERAL, 
//                                  URLValueImpl.create("https://www.google.com", null, "component",
//                                  "value", "")));
//
                ySeries.getTriggers().add(tr1);
//              ySeries.getTriggers().add(tr2);
            }

        }
    }

    protected ColorDefinition getColor(Color color) {
        return ColorDefinitionImpl.create(color.getRed(), color.getGreen(), color.getBlue());
    }
}
