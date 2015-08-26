/*
 *EXHIBIT A - Sun Industry Standards Source License
 *
 *"The contents of this file are subject to the Sun Industry
 *Standards Source License Version 1.2 (the "License");
 *You may not use this file except in compliance with the
 *License. You may obtain a copy of the 
 *License at http://wbemservices.sourceforge.net/license.html
 *
 *Software distributed under the License is distributed on
 *an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either
 *express or implied. See the License for the specific
 *language governing rights and limitations under the License.
 *
 *The Original Code is WBEM Services.
 *
 *The Initial Developer of the Original Code is:
 *Sun Microsystems, Inc.
 *
 *Portions created by: Sun Microsystems, Inc.
 *are Copyright © 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package org.wbemservices.wbem.apps.common;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * <CODE>RowLayout</CODE> lays out components in a single row. The
 * row is as tall as the tallest component.<P>
 *
 * @version 	1.4, 11/17/00
 * @author 	Sun Microsystems
 */
public class RowLayout implements LayoutManager {
  
    LAYOUT_ALIGNMENT align;
    int hgap;
    int vgap;

    /**
     * Constructs a new <CODE>RowLayout</CODE> with a expand alignment.
     */
    public RowLayout() {
	this(LAYOUT_ALIGNMENT.EXPAND, 5, 5);
    }

    /**
     * Constructs a new <CODE>RowLayout</CODE> with the specified alignment.
     *
     * @param alignment The alignment value.
     */
    public RowLayout(LAYOUT_ALIGNMENT alignment) {
	this(alignment, 5, 5);
    }

    /**
     * Constructs a new <CODE>RowLayout</CODE> with the specified
     * alignment and gap values.
     *
     * @param alignment The alignment value.
     * @param hzgap The horizontal gap variable.
     * @param vrtgap The vertical gap variable.
     */
    public RowLayout(LAYOUT_ALIGNMENT alignment, int hzgap, int vrtgap) {
	this.align = alignment;
	this.hgap = hzgap;
	this.vgap = vrtgap;
    }

    public void addLayoutComponent(String name, Component comp) {}
    public void removeLayoutComponent(Component comp) {}

	public int              getHorizontalGap() { return hgap;  }
	public int              getVerticalGap  () { return hgap;  }
	public LAYOUT_ALIGNMENT getAlignment    () { return align; }

    /**
     * Returns the preferred dimensions for this layout given the components
     * in the specified target container.
     *
     * @param target The specified component that is being laid out.
     * @see java.awt.Container
     * @see #minimumLayoutSize(Container)
     */
    public Dimension preferredLayoutSize(Container target) {
	Dimension dim = new Dimension(0, 0);
	int nmembers = target.getComponentCount();

	for (int i = 0; i < nmembers; i++) {
	    Component m = target.getComponent(i);
	    if (m.isVisible()) {
		Dimension d = m.getPreferredSize();
		dim.height = Math.max(dim.height, d.height);
		dim.width += d.width;
	    }
	}
	Insets insets = target.getInsets();
	dim.width  += insets.left + insets.right + (nmembers-1)*hgap;
	dim.height += insets.top + insets.bottom;
	return dim;
    }

    /**
     * Returns the minimum dimensions needed to layout the components
     * contained in the specified target container.
     *
     * @param target The specified component that is being laid out.
     * @see #preferredLayoutSize(Container)
     */
    public Dimension minimumLayoutSize(Container target) {
	Dimension dim = new Dimension(0, 0);
	int nmembers = target.getComponentCount();

	for (int i = 0; i < nmembers; i++) {
	    Component m = target.getComponent(i);
	    if (m.isVisible()) {
		Dimension d = m.getMinimumSize();
		dim.height = Math.max(dim.height, d.height);
		dim.width += d.width;
	    }
	}
	Insets insets = target.getInsets();
	dim.width  += insets.left + insets.right + (nmembers-1)*hgap;
	dim.height += insets.top + insets.bottom;
	return dim;
    }

    /**
     * Lays out the container.  Components are packed horizontally
     * into a single row and made as tall as the tallest component.
     *
     * @param target The specified component that ibeing laid out.
     * @see java.awt.Container
     */
    public void layoutContainer(Container target) {
	Insets insets = target.getInsets();
	Dimension dim = target.getSize();
	int nmembers = target.getComponentCount();
	int x = insets.left, y = insets.top;
	int yoff = 0;
	int tallest = 0;

	// find tallest component
	for (int i = 0; i < nmembers; i++) {
	    Component m = target.getComponent(i);
	    if (m.isVisible()) {
		Dimension d = m.getPreferredSize();
		tallest = Math.max(tallest, d.height);
	    }
	}

	// lay 'em out
	for (int i = 0; i < nmembers; i++) {
	    Component m = target.getComponent(i);
	    Dimension d = m.getPreferredSize();

	    int targetHeight = dim.height - insets.top - insets.bottom;
	    int compHeight   = d.height, compWidth = d.width;

	    if (m.isVisible()) {
		if (align == LAYOUT_ALIGNMENT.LEFT) {
			yoff = 0;
		
		} else if (align == LAYOUT_ALIGNMENT.CENTER) {
			yoff = (targetHeight - compHeight) / 2;
		} else if (align == LAYOUT_ALIGNMENT.RIGHT) {
			yoff = targetHeight - compHeight;
		} else if (align == LAYOUT_ALIGNMENT.EXPAND) {
			yoff = 0;
			compHeight = tallest;
		} else if (align == LAYOUT_ALIGNMENT.FIT) {
			yoff = 0;
			compHeight = targetHeight;
		}
		m.setBounds(x, y+yoff, compWidth, compHeight);
		x += hgap + d.width;
	    }
	}
    }

    /**
     * Returns the string representation of this
     * <CODE>RowLayout</CODE>'s values.
     */
    public String toString() {
	String str = "";
	if (align == LAYOUT_ALIGNMENT.LEFT) {
	    str = ",align=left";
	} else if (align == LAYOUT_ALIGNMENT.CENTER) {
	    str = ",align=center";
	} else if (align == LAYOUT_ALIGNMENT.RIGHT) {
	    str = ",align=right";
	} else if (align == LAYOUT_ALIGNMENT.EXPAND) {
	    str = ",align=expand";
	}
	return getClass().getName() + "[hgap=" + hgap + ",vgap=" + 
	    vgap + str + "]";
    }

}
