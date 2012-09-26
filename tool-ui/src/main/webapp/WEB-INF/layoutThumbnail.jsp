<%@ page import="

com.psddev.cms.db.ContainerSection,
com.psddev.cms.db.HorizontalContainerSection,
com.psddev.cms.db.Page,
com.psddev.cms.db.Section,
com.psddev.cms.tool.ToolPageContext,

java.io.IOException,

java.util.List,
java.util.Map
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

// --- Presentation ---

%><script type="text/javascript">
    $('#<%= request.getAttribute("id") %>').prepend(
            $('<canvas width="<%= _initialWidth %>"'
            + 'height="<%= _initialHeight %>" />'
            ).each(function() {
        if (this.getContext) {
            var c = this.getContext('2d');
            <% renderSection(wp, ((Page.Layout) request.getAttribute("layout")).getOutermostSection(),
                    _initialColor, _padding, 0, 0, _initialWidth,
                    _initialHeight); %>
        }
    }));
</script><%!

//
private static final int _colorDiff = 0x222222;
private static final int _initialColor = 0xffffff - _colorDiff;
private static final int _initialWidth = 100;
private static final int _initialHeight = 150;
private static final int _padding = 4;
private static final double _hSmall2 = 0.3;
private static final double _hSmall3 = 0.25;
private static final double _vSmall2 = 0.2;
private static final double _vSmall3 = 0.1;
private void renderSection(ToolPageContext wp, Section box, int color,
        double padding, double left, double top, double width,
        double height) throws IOException {
    wp.write(String.format("c.fillStyle = '#%s';"
            + "c.fillRect(%f, %f, %f, %f);\n", Integer.toString(color, 16),
            left, top, width, height));

    List<Section> boxes = box instanceof ContainerSection
            ? ((ContainerSection) box).getChildren()
            : null;
    if (boxes == null) {
        return;
    }

    left += padding;
    top += padding;
    width -= 2 * padding;
    height -= 2 * padding;
    int i = 0, s = boxes.size();

    if (box instanceof HorizontalContainerSection) {
        for (Section b : boxes) {
            double w = (width - (s - 1) * padding), l = left;

            if (s == 1) {

            } else if (s == 2) {
                if (i == 0) {
                    w *= _hSmall2;

                } else {
                    l += w * _hSmall2 + padding;
                    w *= (1 - _hSmall2);
                }

            } else {
                if (i == 0) {
                    w *= _hSmall3;

                } else if (i == s - 1) {
                    w *= _hSmall3;
                    l += width - w;

                } else {
                    double tw = w * (1 - _hSmall3 * 2) / (s - 2);
                    l += w * _hSmall3 + padding + (tw + padding) * (i - 1);
                    w = tw;
                }
            }

            renderSection(wp, b, color - _colorDiff, padding, l, top, w, height);
            i ++;
        }
    } else {
        for (Section b : boxes) {
            double h = (height - (s - 1) * padding), t = top;
            if (s == 1) {

            } else if (s == 2) {
                if (i == 0) {
                    h *= _vSmall2;

                } else {
                    t += h * _vSmall2 + padding;
                    h *= (1 - _vSmall2);
                }

            } else {
                if (i == 0) {
                    h *= _vSmall3;

                } else if (i == s - 1) {
                    h *= _vSmall3;
                    t += height - h;

                } else {
                    double th = h * (1 - _vSmall3 * 2) / (s - 2);
                    t += h * _vSmall3 + padding + (th + padding) * (i - 1);
                    h = th;
                }
            }

            renderSection(wp, b, color - _colorDiff, padding, left, t, width, h);
            i ++;
        }
    }
}
%>
