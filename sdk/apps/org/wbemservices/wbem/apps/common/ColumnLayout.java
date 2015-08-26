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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * 
 *
 * @version 	1.4, 11/16/00
 * @author 	Sun Microsystems
 */

public class ColumnLayout implements LayoutManager {
  
    LAYOUT_ALIGNMENT align;
    int hgap;
    int vgap;

    /**
     * Constructs a new <CODE>ColumnLayout</CODE> with an expanded alignment.
     */
    public ColumnLayout() {
	this(LAYOUT_ALIGNMENT.EXPAND, 5, 5);
    }

    /**
     * Constructs a new <CODE>ColumnLayout</CODE> layout with the
     * specified alignment.
     * @param alignment The alignment value.
     */
    public ColumnLayout(LAYOUT_ALIGNMENT alignment) {
	this(alignment, 5, 5);
    }

    /**
     * Constructs a new <CODE>ColumnLayout</CODE> layout with the
     * specified alignment and gap values.
     * @param alignment The alignment value.
     * @param hGap The horizontal gap variable.
     * @param vGap The vertical gap variable.
     */
    public ColumnLayout(LAYOUT_ALIGNMENT alignment, int hGap, int vGap) {
	this.align = alignment;
	this.hgap = hGap;
	this.vgap = vGap;
    }

    public void addLayoutComponent(String name, Component comp) {}
    public void removeLayoutComponent(Component comp) {}

    public int              getHorizontalGap() { return hgap;  }
	public int              getVerticalGap  () { return hgap;  }
	public LAYOUT_ALIGNMENT getAlignment    () { return align; }

    /**
     * Returns the preferred dimensions for this layout given the components
     * in the specified target container.
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
		dim.width = Math.max(dim.width, d.width);
		dim.height += d.height;
	    }
	}
	Insets insets = target.getInsets();
	dim.width  += insets.left + insets.right;
	dim.height += insets.top + insets.bottom + (nmembers-1)*vgap;
	return dim;
    }

    /**
     * Returns the minimum dimensions needed to layout the components
     * contained in the specified target container.
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
		dim.width = Math.max(dim.width, d.width);
		dim.height += d.height;
	    }
	}
	Insets insets = target.getInsets();
	dim.width  += insets.left + insets.right;
	dim.height += insets.top + insets.bottom + (nmembers-1)*vgap;
	return dim;
    }

    /**
     * Lays out the container.  Components are packed vertically
     * into a single column and made as wide as the widest component.
     * @param target The specified component that is being laid out.
     * @see java.awt.Container
     */
    public void layoutContainer(Container target) {
	Insets insets = target.getInsets();
	Dimension dim = target.getSize();
	int nmembers = target.getComponentCount();
	int x = insets.left, y = insets.top;
	int xoff = 0;
	int widest = 0;

	// find widest component
	for (int i = 0; i < nmembers; i++) {
	    Component m = target.getComponent(i);
	    if (m.isVisible()) {
		Dimension d = m.getPreferredSize();
		widest = Math.max(widest, d.width);
	    }
	}

	// lay 'em out
	for (int i = 0; i < nmembers; i++) {
	    Component m = target.getComponent(i);
	    Dimension d = m.getPreferredSize();

	    int targetWidth = dim.width - insets.left - insets.right;
	    int compWidth   = d.width, compHeight = d.height;

	    if (m.isVisible()) {
		if (align == LAYOUT_ALIGNMENT.LEFT) {
		    xoff = 0;
		} else if (align == LAYOUT_ALIGNMENT.CENTER) {
		    xoff = (targetWidth - compWidth) / 2;		
		} else if (align == LAYOUT_ALIGNMENT.RIGHT) {
		    xoff = targetWidth - compWidth;
		} else if (align == LAYOUT_ALIGNMENT.EXPAND) {
		    xoff  = 0;
		    compWidth = widest;
		} else if (align == LAYOUT_ALIGNMENT.FIT) {
		    xoff  = 0;
		    compWidth = targetWidth;
		}
		m.setBounds(x+xoff, y, compWidth, compHeight);
		y += vgap + compHeight;
	    }
	}
    }

    /**
     * Returns the string representation of this
     * <CODE>ColumnLayout</CODE>'s values.
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
	} else if (align == LAYOUT_ALIGNMENT.FIT) {
	    str = ",align=fit";
	}
	return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + 
		str + "]";
    }

}
