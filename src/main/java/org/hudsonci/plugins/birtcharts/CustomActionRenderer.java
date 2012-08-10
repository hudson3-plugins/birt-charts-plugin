
/*
 * The MIT License
 * 
 * Copyright (cursor) 2012, Oracle Corporation
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
package org.hudsonci.plugins.birtcharts;

import org.eclipse.birt.chart.computation.DataPointHints;
import org.eclipse.birt.chart.event.StructureSource;
import org.eclipse.birt.chart.event.StructureType;
import org.eclipse.birt.chart.integrate.SimpleActionHandle;
import org.eclipse.birt.chart.integrate.SimpleActionUtil;
import org.eclipse.birt.chart.model.attribute.ActionType;
import org.eclipse.birt.chart.model.attribute.TooltipValue;
import org.eclipse.birt.chart.model.attribute.URLValue;
import org.eclipse.birt.chart.model.data.Action;
import org.eclipse.birt.chart.render.ActionRendererAdapter;

/**
 * Custom Action Renderer (Tooltip & URL redirect) while creating ImageMap for a
 * BIRT Chart
 *
 * @author Winston Prakash
 */
public class CustomActionRenderer extends ActionRendererAdapter {

    @Override
    public void processAction(Action action, StructureSource source) {

        if (ActionType.SHOW_TOOLTIP_LITERAL.equals(action.getType())) {
            TooltipValue tv = (TooltipValue) action.getValue();
            if (StructureType.SERIES_DATA_POINT.equals(source.getType())) {
                if ((tv.getText() == null) || ("".equals(tv.getText()))) {
                    final DataPointHints dph = (DataPointHints) source.getSource();
                    int value = Integer.parseInt(dph.getDisplayValue().trim());
                    if (value > 0) {
                        String MyToolTip = dph.getBaseDisplayValue() + ": " + dph.getDisplayValue();
                        tv.setText(MyToolTip);
                    } else {
                        tv.setText("");
                    }
                }
            }
        }
        if (ActionType.URL_REDIRECT_LITERAL.equals(action.getType())) {
            URLValue uv = (URLValue) action.getValue();

            String sa = uv.getBaseUrl();
            if (sa != null) {
                SimpleActionHandle handle = SimpleActionUtil.deserializeAction(sa);
                String uri = handle.getURI();
                if (StructureType.SERIES_DATA_POINT.equals(source.getType())) {
                    final DataPointHints dph = (DataPointHints) source.getSource();
                    uri = uv.getBaseUrl();
                    String basedisply = dph.getBaseDisplayValue();

                    uri = replaceTemplate(uri, Integer.parseInt(basedisply.trim().replaceAll("#", "")));
                }
                uv.setBaseUrl(uri);
                uv.setTarget(handle.getTargetWindow());
            }
        }
    }

    private String replaceTemplate(String baseUrl, int buildNo) {
        int index1 = baseUrl.indexOf("${");
        int index2 = baseUrl.indexOf("}");
        if ((index1 > 0) && (index2 > 0)) {
            String prefix = baseUrl.substring(0, index1 - 1);
            String suffix = baseUrl.substring(index2 + 1);
            baseUrl = prefix + buildNo + suffix;
        }
        return baseUrl;
    }
}
