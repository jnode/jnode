/* MetalLookAndFeel.java
   Copyright (C) 2002, 2005 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package javax.swing.plaf.metal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.BorderUIResource.LineBorderUIResource;
import javax.swing.plaf.basic.BasicLookAndFeel;


/**
 * A custom look and feel that is designed to look similar across different
 * operating systems.
 */
public class MetalLookAndFeel extends BasicLookAndFeel
{	   
  private static final long serialVersionUID = 6680646159193457980L;
  
  /** The current theme. */
  private static MetalTheme theme;
  
  /** The look and feel defaults. */
  private UIDefaults LAF_defaults;

  /**
   * Creates a new instance of the Metal look and feel.
   */
  public MetalLookAndFeel()
  {
    if (theme == null)
      createDefaultTheme();
  }

  /**
   * Sets the current theme to a new instance of {@link DefaultMetalTheme}.
   */
  protected void createDefaultTheme()
  {
    setCurrentTheme(new DefaultMetalTheme());
  }

  /**
   * Returns <code>false</code> to indicate that this look and feel does not
   * attempt to emulate the look and feel of native applications on the host
   * platform.
   * 
   * @return <code>false</code>.
   */
  public boolean isNativeLookAndFeel()
  {
    return false;
  }

  /**
   * Returns <code>true</code> to indicate that this look and feel is supported
   * on all platforms.
   * 
   * @return <code>true</code>.
   */
  public boolean isSupportedLookAndFeel()
  {
    return true;
  }

  /**
   * Returns a string describing the look and feel.  In this case, the method
   * returns "Metal look and feel".
   * 
   * @return A string describing the look and feel.
   */
  public String getDescription()
  {
    return "Metal look and feel";
  }

  /**
   * Returns the look and feel identifier.
   * 
   * @return "MetalLookAndFeel".
   */
  public String getID()
  {
    return "MetalLookAndFeel";
  }

  /**
   * Returns the look and feel name.
   * 
   * @return "MetalLookAndFeel".
   */
  public String getName()
  {
    return "MetalLookAndFeel";
  }

  public UIDefaults getDefaults()
  {
    if (LAF_defaults == null)
      {
        LAF_defaults = super.getDefaults();

        // add custom theme entries to the table
        theme.addCustomEntriesToTable(LAF_defaults);
      }
    
    // Returns the default values for this look and feel. 
    return LAF_defaults;
  }

  /**
   * Returns the accelerator foreground color from the installed theme.
   * 
   * @return The accelerator foreground color.
   */
  public static ColorUIResource getAcceleratorForeground()
  {
    return theme.getAcceleratorForeground();
  }

  /**
   * Returns the accelerator selected foreground color from the installed 
   * theme.
   * 
   * @return The accelerator selected foreground color.
   */
  public static ColorUIResource getAcceleratorSelectedForeground()
  {
    return theme.getAcceleratorSelectedForeground();
  }

  /**
   * Returns the color black from the installed theme.
   * 
   * @return The color black.
   */
  public static ColorUIResource getBlack()
  {
    return theme.getBlack();
  }

  /**
   * Returns the control color from the installed theme.
   * 
   * @return The control color.
   */
  public static ColorUIResource getControl()
  {
    return theme.getControl();
  }

  /**
   * Returns the color used for dark shadows on controls, from the installed
   * theme.
   * 
   * @return The color used for dark shadows on controls.
   */
  public static ColorUIResource getControlDarkShadow()
  {
    return theme.getControlDarkShadow();
  }

  /**
   * Returns the color used for disabled controls, from the installed theme.
   * 
   * @return The color used for disabled controls.
   */
  public static ColorUIResource getControlDisabled()
  {
    return theme.getControlDisabled();
  }

  /**
   * Returns the color used to draw highlights for controls, from the installed
   * theme.
   * 
   * @return The color used to draw highlights for controls.
   */
  public static ColorUIResource getControlHighlight()
  {
    return theme.getControlHighlight();
  }

  /**
   * Returns the color used to display control info, from the installed 
   * theme.
   * 
   * @return The color used to display control info.
   */
  public static ColorUIResource getControlInfo()
  {
    return theme.getControlInfo();
  }

  /**
   * Returns the color used to draw shadows for controls, from the installed
   * theme.
   * 
   * @return The color used to draw shadows for controls.
   */
  public static ColorUIResource getControlShadow()
  {
    return theme.getControlShadow();
  }

  /**
   * Returns the color used for text on controls, from the installed theme.
   * 
   * @return The color used for text on controls.
   */
  public static ColorUIResource getControlTextColor()
  {
    return theme.getControlTextColor();
  }

  /**
   * Returns the font used for text on controls, from the installed theme.
   * 
   * @return The font used for text on controls.
   */
  public static FontUIResource getControlTextFont()
  {
    return theme.getControlTextFont();
  }

  /**
   * Returns the color used for the desktop background, from the installed 
   * theme.
   * 
   * @return The color used for the desktop background.
   */
  public static ColorUIResource getDesktopColor()
  {
    return theme.getDesktopColor();
  }

  /**
   * Returns the color used to draw focus highlights, from the installed 
   * theme.
   * 
   * @return The color used to draw focus highlights.
   */
  public static ColorUIResource getFocusColor()
  {
    return theme.getFocusColor();
  }

  /**
   * Returns the color used to draw highlighted text, from the installed
   * theme.
   * 
   * @return The color used to draw highlighted text.
   */
  public static ColorUIResource getHighlightedTextColor()
  {
    return theme.getHighlightedTextColor();
  }

  /**
   * Returns the color used to draw text on inactive controls, from the
   * installed theme.
   * 
   * @return The color used to draw text on inactive controls.
   */
  public static ColorUIResource getInactiveControlTextColor()
  {
    return theme.getInactiveControlTextColor();
  }

  /**
   * Returns the color used to draw inactive system text, from the installed
   * theme.
   * 
   * @return The color used to draw inactive system text.
   */
  public static ColorUIResource getInactiveSystemTextColor()
  {
    return theme.getInactiveSystemTextColor();
  }

  /**
   * Returns the background color for menu items, from the installed theme.
   * 
   * @return The background color for menu items.
   * 
   * @see #getMenuSelectedBackground()
   */
  public static ColorUIResource getMenuBackground()
  {
    return theme.getMenuBackground();
  }

  /**
   * Returns the foreground color for disabled menu items, from the installed
   * theme.
   * 
   * @return The foreground color for disabled menu items.
   * 
   * @see #getMenuForeground()
   */
  public static ColorUIResource getMenuDisabledForeground()
  {
    return theme.getMenuDisabledForeground();
  }

  /**
   * Returns the foreground color for menu items, from the installed theme.
   * 
   * @return The foreground color for menu items.
   * 
   * @see #getMenuDisabledForeground()
   * @see #getMenuSelectedForeground()
   */
  public static ColorUIResource getMenuForeground()
  {
    return theme.getMenuForeground();
  }

  /**
   * Returns the background color for selected menu items, from the installed
   * theme.
   * 
   * @return The background color for selected menu items.
   * 
   * @see #getMenuBackground()
   */
  public static ColorUIResource getMenuSelectedBackground()
  {
    return theme.getMenuSelectedBackground();
  }

  /**
   * Returns the foreground color for selected menu items, from the installed
   * theme.
   * 
   * @return The foreground color for selected menu items.
   * 
   * @see #getMenuForeground()
   */
  public static ColorUIResource getMenuSelectedForeground()
  {
    return theme.getMenuSelectedForeground();
  }

  /**
   * Returns the font used for text in menus, from the installed theme.
   * 
   * @return The font used for text in menus.
   */
  public static FontUIResource getMenuTextFont()
  {
    return theme.getMenuTextFont();
  }

  /**
   * Returns the primary color for controls, from the installed theme.
   * 
   * @return The primary color for controls.
   */
  public static ColorUIResource getPrimaryControl()
  {
    return theme.getPrimaryControl();
  }

  /**
   * Returns the primary color for the dark shadow on controls, from the 
   * installed theme.
   * 
   * @return The primary color for the dark shadow on controls.
   */
  public static ColorUIResource getPrimaryControlDarkShadow()
  {
    return theme.getPrimaryControlDarkShadow();
  }

  /**
   * Returns the primary color for the highlight on controls, from the 
   * installed theme.
   * 
   * @return The primary color for the highlight on controls.
   */
  public static ColorUIResource getPrimaryControlHighlight()
  {
    return theme.getPrimaryControlHighlight();
  }

  /**
   * Returns the primary color for the information on controls, from the 
   * installed theme.
   * 
   * @return The primary color for the information on controls.
   */
  public static ColorUIResource getPrimaryControlInfo()
  {
    return theme.getPrimaryControlInfo();
  }

  /**
   * Returns the primary color for the shadow on controls, from the installed
   * theme.
   * 
   * @return The primary color for the shadow on controls.
   */
  public static ColorUIResource getPrimaryControlShadow()
  {
    return theme.getPrimaryControlShadow();
  }

  /**
   * Returns the background color for separators, from the installed theme.
   * 
   * @return The background color for separators.
   */
  public static ColorUIResource getSeparatorBackground()
  {
    return theme.getSeparatorBackground();
  }

  /**
   * Returns the foreground color for separators, from the installed theme.
   * 
   * @return The foreground color for separators.
   */
  public static ColorUIResource getSeparatorForeground()
  {
    return theme.getSeparatorForeground();
  }

  /**
   * Returns the font used for sub text, from the installed theme.
   * 
   * @return The font used for sub text.
   */
  public static FontUIResource getSubTextFont()
  {
    return theme.getSubTextFont();
  }

  /**
   * Returns the color used for system text, from the installed theme.
   * 
   * @return The color used for system text.
   */
  public static ColorUIResource getSystemTextColor()
  {
    return theme.getSystemTextColor();
  }

  /**
   * Returns the font used for system text, from the installed theme.
   * 
   * @return The font used for system text.
   */
  public static FontUIResource getSystemTextFont()
  {
    return theme.getSystemTextFont();
  }

  /**
   * Returns the color used to highlight text, from the installed theme.
   * 
   * @return The color used to highlight text.
   */
  public static ColorUIResource getTextHighlightColor()
  {
    return theme.getTextHighlightColor();
  }

  /**
   * Returns the color used to display user text, from the installed theme.
   * 
   * @return The color used to display user text.
   */
  public static ColorUIResource getUserTextColor()
  {
    return theme.getUserTextColor();
  }

  /**
   * Returns the font used for user text, obtained from the current theme.
   * 
   * @return The font used for user text.
   */
  public static FontUIResource getUserTextFont()
  {
    return theme.getUserTextFont();
  }

  /**
   * Returns the color used for white, from the installed theme.
   * 
   * @return The color used for white.
   */
  public static ColorUIResource getWhite()
  {
    return theme.getWhite();
  }

  /**
   * Returns the window background color, from the installed theme.
   * 
   * @return The window background color.
   */
  public static ColorUIResource getWindowBackground()
  {
    return theme.getWindowBackground();
  }

  /**
   * Returns the window title background color, from the installed theme.
   * 
   * @return The window title background color.
   */
  public static ColorUIResource getWindowTitleBackground()
  {
    return theme.getWindowTitleBackground();
  }

  /**
   * Returns the window title font from the current theme.
   * 
   * @return The window title font.
   * 
   * @see MetalTheme
   */
  public static FontUIResource getWindowTitleFont()
  {
    return theme.getWindowTitleFont();
  }

  /**
   * Returns the window title foreground color, from the installed theme.
   * 
   * @return The window title foreground color.
   */
  public static ColorUIResource getWindowTitleForeground()
  {
    return theme.getWindowTitleForeground();
  }

  /**
   * Returns the background color for an inactive window title, from the 
   * installed theme.
   * 
   * @return The background color for an inactive window title.
   */
  public static ColorUIResource getWindowTitleInactiveBackground()
  {
    return theme.getWindowTitleInactiveBackground();
  }

  /**
   * Returns the foreground color for an inactive window title, from the 
   * installed theme.
   * 
   * @return The foreground color for an inactive window title.
   */
  public static ColorUIResource getWindowTitleInactiveForeground()
  {
    return theme.getWindowTitleInactiveForeground();
  }

  /**
   * Sets the current theme for the look and feel.  Note that the theme must be 
   * set <em>before</em> the look and feel is installed.  To change the theme 
   * for an already running application that is using the 
   * {@link MetalLookAndFeel}, first set the theme with this method, then 
   * create a new instance of {@link MetalLookAndFeel} and install it in the 
   * usual way (see {@link UIManager#setLookAndFeel(LookAndFeel)}).
   * 
   * @param theme  the theme (<code>null</code> not permitted).
   * 
   * @throws NullPointerException if <code>theme</code> is <code>null</code>.
   */
  public static void setCurrentTheme(MetalTheme theme)
  {
    if (theme == null)
      throw new NullPointerException("Null 'theme' not permitted.");
    MetalLookAndFeel.theme = theme;
  }

  /**
   * Sets the ComponentUI classes for all Swing components to the Metal
   * implementations.
   *
   * In particular this sets the following keys:
   *
   * <table>
   * <tr>
   * <th>Key</th><th>Value</th>
   * </tr><tr>
   * <td>ButtonUI</td><td>{@link MetalButtonUI}</td>
   * </tr><tr>
   * <td>CheckBoxUI</td><td>{@link MetalCheckBoxUI}</td>
   * </tr><tr>
   * <td>ComboBoxUI</td><td>{@link MetalComboBoxUI}</td>
   * </tr><tr>
   * <td>DesktopIconUI</td><td>{@link MetalDesktopIconUI}</td>
   * </tr><tr>
   * <td>InternalFrameUI</td><td>{@link MetalInternalFrameUI}</td>
   * </tr><tr>
   * <td>LabelUI</td><td>{@link MetalLabelUI}</td>
   * </tr><tr>
   * <td>PopupMenuSeparatorUI</td><td>{@link MetalPopupMenuSeparatorUI}</td>
   * </tr><tr>
   * <td>ProgressBarUI</td><td>{@link MetalProgressBarUI}</td>
   * </tr><tr>
   * <td>RadioButtonUI</td><td>{@link MetalRadioButtonUI}</td>
   * </tr><tr>
   * <td>RootPaneUI</td><td>{@link MetalRootPaneUI}</td>
   * </tr><tr>
   * <td>ScrollBarUI</td><td>{@link MetalScrollBarUI}</td>
   * </tr><tr>
   * <td>ScrollPaneUI</td><td>{@link MetalScrollPaneUI}</td>
   * </tr><tr>
   * <td>SeparatorUI</td><td>{@link MetalSeparatorUI}</td>
   * </tr><tr>
   * <td>SliderUI</td><td>{@link MetalSliderUI}</td>
   * </tr><tr>
   * <td>SplitPaneUI</td><td>{@link MetalSplitPaneUI}</td>
   * </tr><tr>
   * <td>TabbedPaneUI</td><td>{@link MetalTabbedPaneUI}</td>
   * </tr><tr>
   * <td>TextFieldUI</td><td>{@link MetalTextFieldUI}</td>
   * </tr><tr>
   * <td>ToggleButtonUI</td><td>{@link MetalToggleButtonUI}</td>
   * </tr><tr>
   * <td>ToolBarUI</td><td>{@link MetalToolBarUI}</td>
   * </tr><tr>
   * <td>ToolTipUI</td><td>{@link MetalToolTipUI}</td>
   * </tr><tr>
   * <td>TreeUI</td><td>{@link MetalTreeUI}</td>
   * </tr><tr>
   * </table>
   *
   * @param defaults the UIDefaults where the class defaults are added
   */
  protected void initClassDefaults(UIDefaults defaults)
  {
    super.initClassDefaults(defaults);

    // Variables
    Object[] uiDefaults;
    // Initialize Class Defaults
    uiDefaults = new Object[] {
      "ButtonUI", "javax.swing.plaf.metal.MetalButtonUI",
      "CheckBoxUI", "javax.swing.plaf.metal.MetalCheckBoxUI",
      "ComboBoxUI", "javax.swing.plaf.metal.MetalComboBoxUI",
      "DesktopIconUI", "javax.swing.plaf.metal.MetalDesktopIconUI",
      "InternalFrameUI", "javax.swing.plaf.metal.MetalInternalFrameUI",
      "LabelUI", "javax.swing.plaf.metal.MetalLabelUI",
      "MenuBarUI", "javax.swing.plaf.metal.MetalMenuBarUI",
      "PopupMenuSeparatorUI",
      "javax.swing.plaf.metal.MetalPopupMenuSeparatorUI",
      "ProgressBarUI", "javax.swing.plaf.metal.MetalProgressBarUI",
      "RadioButtonUI", "javax.swing.plaf.metal.MetalRadioButtonUI",
      "RootPaneUI", "javax.swing.plaf.metal.MetalRootPaneUI",
      "ScrollBarUI", "javax.swing.plaf.metal.MetalScrollBarUI",
      "ScrollPaneUI", "javax.swing.plaf.metal.MetalScrollPaneUI",
      "SeparatorUI", "javax.swing.plaf.metal.MetalSeparatorUI",
      "SliderUI", "javax.swing.plaf.metal.MetalSliderUI",
      "SplitPaneUI", "javax.swing.plaf.metal.MetalSplitPaneUI",
      "TabbedPaneUI", "javax.swing.plaf.metal.MetalTabbedPaneUI",
      "TextFieldUI", "javax.swing.plaf.metal.MetalTextFieldUI",
      "ToggleButtonUI", "javax.swing.plaf.metal.MetalToggleButtonUI",
      "ToolBarUI", "javax.swing.plaf.metal.MetalToolBarUI",
      "ToolTipUI", "javax.swing.plaf.metal.MetalToolTipUI",
      "TreeUI", "javax.swing.plaf.metal.MetalTreeUI",
    };
    // Add Class Defaults to UI Defaults table
    defaults.putDefaults(uiDefaults);
  }

  /**
   * Initializes the component defaults for the Metal Look &amp; Feel.
   *
   * In particular this sets the following keys (the colors are given
   * as RGB hex values):
   *
   * <table>
   * <tr>
   * <th>Key</th><th>Value</th>
   * </tr><tr>
   * <td>Button.background</td><td>0xcccccc</td>
   * </tr><tr>
   * <td>Button.border</td><td>{@link MetalBorders#getButtonBorder()}</td>
   * </tr><tr>
   * <td>Button.font</td><td>{@link #getControlTextFont}</td>
   * </tr><tr>
   * <td>Button.margin</td><td><code>new java.awt.Insets(2, 14, 2, 14)</code>
   * </td>
   * </tr><tr>
   * <td>CheckBox.background</td><td>0xcccccc</td>
   * </tr><tr>
   * <td>CheckBoxMenuItem.background</td><td>0xcccccc</td>
   * </tr><tr>
   * <td>ToolBar.background</td><td>0xcccccc</td>
   * </tr><tr>
   * <td>Panel.background</td><td>0xcccccc</td>
   * </tr><tr>
   * <td>Slider.background</td><td>0xcccccc</td>
   * </tr><tr>
   * <td>OptionPane.background</td><td>0xcccccc</td>
   * </tr><tr>
   * <td>ProgressBar.background</td><td>0xcccccc</td>
   * </tr><tr>
   * <td>TabbedPane.background</td><td>0xcccccc</td>
   * </tr><tr>
   * <td>Label.background</td><td>0xcccccc</td>
   * </tr><tr>
   * <td>Label.font</td><td>{@link #getControlTextFont}</td>
   * </tr><tr>
   * <td>Menu.background</td><td>0xcccccc</td>
   * </tr><tr>
   * <td>MenuBar.background</td><td>0xcccccc</td>
   * </tr><tr>
   * <td>MenuItem.background</td><td>0xcccccc</td>
   * </tr><tr>
   * <td>ScrollBar.background</td><td>0xcccccc</td>
   * </tr><tr>
   * <td>PopupMenu.border</td>
   * <td><code>new javax.swing.plaf.metal.MetalBorders.PopupMenuBorder()</td>
   * </tr><tr>
   * </table>
   *
   * @param defaults the UIDefaults instance to which the values are added
   */
  protected void initComponentDefaults(UIDefaults defaults)
  {
    super.initComponentDefaults(defaults);
    Object[] myDefaults = new Object[] {
      "Button.background", getControl(),
      "Button.border", MetalBorders.getButtonBorder(),
      "Button.darkShadow", getControlDarkShadow(),
      "Button.disabledText", getInactiveControlTextColor(),
      "Button.focus", getFocusColor(),
      "Button.font", getControlTextFont(),
      "Button.foreground", getControlTextColor(),
      "Button.highlight", getControlHighlight(),
      "Button.light", getControlHighlight(),
      "Button.margin", new InsetsUIResource(2, 14, 2, 14),
      "Button.select", getControlShadow(),
      "Button.shadow", getControlShadow(),

      "CheckBox.background", getControl(),
      "CheckBox.border", MetalBorders.getButtonBorder(),
      "CheckBox.disabledText", getInactiveControlTextColor(),
      "CheckBox.focus", getFocusColor(),
      "CheckBox.font", new FontUIResource("Dialog", Font.BOLD, 12),
      "CheckBox.foreground", getControlTextColor(),
      "CheckBox.icon",
      new UIDefaults.ProxyLazyValue
          ("javax.swing.plaf.metal.MetalCheckBoxIcon"),
      "CheckBox.checkIcon",
      new UIDefaults.ProxyLazyValue
      ("javax.swing.plaf.metal.MetalCheckBoxIcon"),
      "Checkbox.select", getControlShadow(),

      "CheckBoxMenuItem.acceleratorFont", new FontUIResource("Dialog", Font.PLAIN, 10),
      "CheckBoxMenuItem.acceleratorForeground", getAcceleratorForeground(),
      "CheckBoxMenuItem.acceleratorSelectionForeground", getAcceleratorSelectedForeground(),
      "CheckBoxMenuItem.background", getMenuBackground(),
      "CheckBoxMenuItem.borderPainted", new Boolean(true),
      "CheckBoxMenuItem.commandSound", "sounds/MenuItemCommand.wav",
      "CheckBoxMenuItem.checkIcon", MetalIconFactory.getCheckBoxMenuItemIcon(),
      "CheckBoxMenuItem.disabledForeground", getMenuDisabledForeground(),
      "CheckBoxMenuItem.font", new FontUIResource("Dialog", Font.BOLD, 12),
      "CheckBoxMenuItem.foreground", getMenuForeground(),
      "CheckBoxMenuItem.selectionBackground", getMenuSelectedBackground(),
      "CheckBoxMenuItem.selectionForeground", getMenuSelectedForeground(),

      "ColorChooser.background", getControl(),
      "ColorChooser.foreground", getControlTextColor(),
      "ColorChooser.rgbBlueMnemonic", new Integer(0),
      "ColorChooser.rgbGreenMnemonic", new Integer(0),
      "ColorChooser.rgbRedMnemonic", new Integer(0),
      "ColorChooser.swatchesDefaultRecentColor", getControl(),

      "ComboBox.background", getControl(),
      "ComboBox.buttonBackground", getControl(),
      "ComboBox.buttonDarkShadow", getControlDarkShadow(),
      "ComboBox.buttonHighlight", getControlHighlight(),
      "ComboBox.buttonShadow", getControlShadow(),
      "ComboBox.disabledBackground", getControl(),
      "ComboBox.disabledForeground", getInactiveSystemTextColor(),
      "ComboBox.font", new FontUIResource("Dialog", Font.BOLD, 12),
      "ComboBox.foreground", getControlTextColor(),
      "ComboBox.selectionBackground", getPrimaryControlShadow(),
      "ComboBox.selectionForeground", getControlTextColor(),

      "Desktop.background", getDesktopColor(),

      "DesktopIcon.background", getControl(),
      "DesktopIcon.foreground", getControlTextColor(),
      "DesktopIcon.width", new Integer(160),
      "DesktopIcon.border", MetalBorders.getDesktopIconBorder(),

      "EditorPane.background", getWindowBackground(),
      "EditorPane.caretForeground", getUserTextColor(),
      "EditorPane.font", new FontUIResource("Dialog", Font.PLAIN, 12),
      "EditorPane.foreground",  getUserTextColor(),
      "EditorPane.inactiveForeground",  getInactiveSystemTextColor(),
      "EditorPane.selectionBackground", getTextHighlightColor(),
      "EditorPane.selectionForeground", getHighlightedTextColor(),
      
      "FormattedTextField.background", getWindowBackground(),
      "FormattedTextField.border",
      new BorderUIResource(MetalBorders.getTextFieldBorder()),
      "FormattedTextField.caretForeground", getUserTextColor(),
      "FormattedTextField.font", new FontUIResource("Dialog", Font.PLAIN, 12),
      "FormattedTextField.foreground",  getUserTextColor(),
      "FormattedTextField.inactiveBackground",  getControl(),
      "FormattedTextField.inactiveForeground",  getInactiveSystemTextColor(),
      "FormattedTextField.selectionBackground", getTextHighlightColor(),
      "FormattedTextField.selectionForeground", getHighlightedTextColor(),

      "FileView.computerIcon", MetalIconFactory.getTreeComputerIcon(),
      "FileView.directoryIcon", MetalIconFactory.getTreeFolderIcon(),
      "FileView.fileIcon", MetalIconFactory.getTreeLeafIcon(),
      "FileView.floppyDriveIcon", MetalIconFactory.getTreeFloppyDriveIcon(),
      "FileView.hardDriveIcon", MetalIconFactory.getTreeHardDriveIcon(),

      "InternalFrame.activeTitleBackground", getWindowTitleBackground(),
      "InternalFrame.activeTitleForeground", getWindowTitleForeground(),
      "InternalFrame.border", new MetalBorders.InternalFrameBorder(),
      "InternalFrame.borderColor", getControl(),
      "InternalFrame.borderDarkShadow", getControlDarkShadow(),
      "InternalFrame.borderHighlight", getControlHighlight(),
      "InternalFrame.borderLight", getControlHighlight(),
      "InternalFrame.borderShadow", getControlShadow(),
      "InternalFrame.icon", MetalIconFactory.getInternalFrameDefaultMenuIcon(),
      "InternalFrame.closeIcon", 
        MetalIconFactory.getInternalFrameCloseIcon(16),
      "InternalFrame.inactiveTitleBackground", getWindowTitleInactiveBackground(),
      "InternalFrame.inactiveTitleForeground", getWindowTitleInactiveForeground(),
      "InternalFrame.maximizeIcon", 
        MetalIconFactory.getInternalFrameMaximizeIcon(16),
      "InternalFrame.iconifyIcon", 
        MetalIconFactory.getInternalFrameMinimizeIcon(16),
      "InternalFrame.paletteBorder", new MetalBorders.PaletteBorder(),
      "InternalFrame.paletteCloseIcon", new MetalIconFactory.PaletteCloseIcon(),
      "InternalFrame.paletteTitleHeight", new Integer(11),

      "Label.background", getControl(),
      "Label.disabledForeground", getInactiveSystemTextColor(),
      "Label.disabledShadow", getControlShadow(),
      "Label.font", getControlTextFont(),
      "Label.foreground", getSystemTextColor(),

      "List.font", getControlTextFont(),
      "List.background", getWindowBackground(),
      "List.foreground", getUserTextColor(),
      "List.selectionBackground", getTextHighlightColor(),
      "List.selectionForeground", getHighlightedTextColor(),
      "List.focusCellHighlightBorder", 
        new LineBorderUIResource(MetalLookAndFeel.getFocusColor()),

      "Menu.acceleratorFont", new FontUIResource("Dialog", Font.PLAIN, 10),
      "Menu.acceleratorForeground", getAcceleratorForeground(),
      "Menu.acceleratorSelectionForeground", getAcceleratorSelectedForeground(),
      "Menu.background", getMenuBackground(),
      "Menu.border", new MetalBorders.MenuItemBorder(),
      "Menu.borderPainted", Boolean.TRUE,
      "Menu.disabledForeground", getMenuDisabledForeground(),
      "Menu.font", getControlTextFont(),
      "Menu.foreground", getMenuForeground(),
      "Menu.selectionBackground", getMenuSelectedBackground(),
      "Menu.selectionForeground", getMenuSelectedForeground(),

      "MenuBar.background", getMenuBackground(),
      "MenuBar.border", new MetalBorders.MenuBarBorder(),
      "MenuBar.font", getControlTextFont(),
      "MenuBar.foreground", getMenuForeground(),
      "MenuBar.highlight", getControlHighlight(),
      "MenuBar.shadow", getControlShadow(),

      "MenuItem.acceleratorFont", new FontUIResource("Dialog", Font.PLAIN, 10),
      "MenuItem.acceleratorForeground", getAcceleratorForeground(),
      "MenuItem.acceleratorSelectionForeground", getAcceleratorSelectedForeground(),
      "MenuItem.background", getMenuBackground(),
      "MenuItem.border", new MetalBorders.MenuItemBorder(),
      "MenuItem.disabledForeground", getMenuDisabledForeground(),
      "MenuItem.font", getControlTextFont(),
      "MenuItem.foreground", getMenuForeground(),
      "MenuItem.selectionBackground", getMenuSelectedBackground(),
      "MenuItem.selectionForeground", getMenuSelectedForeground(),

      "OptionPane.background", getControl(),
      "OptionPane.errorDialog.border.background", new ColorUIResource(153, 51, 51), 
      "OptionPane.errorDialog.titlePane.background", new ColorUIResource(255, 153, 153),
      "OptionPane.errorDialog.titlePane.foreground", new ColorUIResource(51, 0, 0),
      "OptionPane.errorDialog.titlePane.shadow", new ColorUIResource(204, 102, 102),
      "OptionPane.foreground", getControlTextColor(),
      "OptionPane.messageForeground", getControlTextColor(),
      "OptionPane.questionDialog.border.background", new ColorUIResource(51, 102, 51),
      "OptionPane.questionDialog.titlePane.background", new ColorUIResource(153, 204, 153),
      "OptionPane.questionDialog.titlePane.foreground", new ColorUIResource(0, 51, 0),
      "OptionPane.questionDialog.titlePane.shadow", new ColorUIResource(102, 153, 102),
      "OptionPane.warningDialog.border.background", new ColorUIResource(153, 102, 51),
      "OptionPane.warningDialog.titlePane.background", new ColorUIResource(255, 204, 153),
      "OptionPane.warningDialog.titlePane.foreground", new ColorUIResource(102, 51, 0),
      "OptionPane.warningDialog.titlePane.shadow", new ColorUIResource(204, 153, 102),

      "Panel.background", getControl(),
      "Panel.foreground", getUserTextColor(),

      "PasswordField.background", getWindowBackground(),
      "PasswordField.border",
      new BorderUIResource(MetalBorders.getTextFieldBorder()),
      "PasswordField.caretForeground", getUserTextColor(),
      "PasswordField.foreground", getUserTextColor(),
      "PasswordField.inactiveBackground", getControl(),
      "PasswordField.inactiveForeground", getInactiveSystemTextColor(),
      "PasswordField.selectionBackground", getTextHighlightColor(),
      "PasswordField.selectionForeground", getHighlightedTextColor(),

      "PopupMenu.background", getMenuBackground(),
      "PopupMenu.border", new MetalBorders.PopupMenuBorder(),
      "PopupMenu.font", new FontUIResource("Dialog", Font.BOLD, 12),
      "PopupMenu.foreground", getMenuForeground(),

      "ProgressBar.background", getControl(),
      "ProgressBar.border", new BorderUIResource.LineBorderUIResource(getControlDarkShadow(), 1),
      "ProgressBar.font", new FontUIResource("Dialog", Font.BOLD, 12),
      "ProgressBar.foreground", getPrimaryControlShadow(),
      "ProgressBar.selectionBackground", getPrimaryControlDarkShadow(),
      "ProgressBar.selectionForeground", getControl(),

      "RadioButton.background", getControl(),
      "RadioButton.darkShadow", getControlDarkShadow(),
      "RadioButton.disabledText", getInactiveControlTextColor(),
      "RadioButton.icon",
      new UIDefaults.LazyValue()
      {
        public Object createValue(UIDefaults def)
          {
            return MetalIconFactory.getRadioButtonIcon();
          }
      },
      "RadioButton.focus", MetalLookAndFeel.getFocusColor(),
      "RadioButton.font", MetalLookAndFeel.getControlTextFont(),
      "RadioButton.foreground", getControlTextColor(),
      "RadioButton.highlight", getControlHighlight(),
      "RadioButton.light", getControlHighlight(),
      "RadioButton.select", getControlShadow(),
      "RadioButton.shadow", getControlShadow(),

      "RadioButtonMenuItem.acceleratorFont", new Font("Dialog", Font.PLAIN, 10),
      "RadioButtonMenuItem.acceleratorForeground", getAcceleratorForeground(),
      "RadioButtonMenuItem.acceleratorSelectionForeground", getAcceleratorSelectedForeground(),
      "RadioButtonMenuItem.background", getMenuBackground(),
      "RadioButtonMenuItem.border", new MetalBorders.MenuItemBorder(),
      "RadioButtonMenuItem.borderPainted", Boolean.TRUE,
      "RadioButtonMenuItem.checkIcon", 
        MetalIconFactory.getRadioButtonMenuItemIcon(),
      "RadioButtonMenuItem.disabledForeground", getMenuDisabledForeground(),
      "RadioButtonMenuItem.font", MetalLookAndFeel.getControlTextFont(),
      "RadioButtonMenuItem.foreground", getMenuForeground(),
      "RadioButtonMenuItem.margin", new InsetsUIResource(2, 2, 2, 2),
      "RadioButtonMenuItem.selectionBackground", 
        MetalLookAndFeel.getMenuSelectedBackground(),
      "RadioButtonMenuItem.selectionForeground", 
        MetalLookAndFeel.getMenuSelectedForeground(),

      "ScrollBar.background", getControl(),
      "ScrollBar.darkShadow", getControlDarkShadow(),
      "ScrollBar.foreground", getControl(),
      "ScrollBar.highlight", getControlHighlight(),
      "ScrollBar.shadow", getControlShadow(),
      "ScrollBar.thumb", getPrimaryControlShadow(),
      "ScrollBar.thumbDarkShadow", getControlDarkShadow(),
      "ScrollBar.thumbHighlight", getPrimaryControl(),
      "ScrollBar.thumbShadow", getPrimaryControlDarkShadow(),
      "ScrollBar.track", getControl(),
      "ScrollBar.trackHighlight", getControlDarkShadow(),
      "ScrollBar.width", new Integer(17),

      "ScrollPane.background", getControl(),
      "ScrollPane.border", new MetalBorders.ScrollPaneBorder(),
      "ScrollPane.foreground", getControlTextColor(),

      "Separator.background", getSeparatorBackground(),
      "Separator.foreground", getSeparatorForeground(),
      "Separator.highlight", getControlHighlight(),
      "Separator.shadow", getControlShadow(),

      "Slider.background", getControl(),
      "Slider.focus", getFocusColor(),
      "Slider.focusInsets", new InsetsUIResource(0, 0, 0, 0),
      "Slider.foreground", getPrimaryControlShadow(),
      "Slider.highlight", getControlHighlight(),
      "Slider.horizontalThumbIcon", 
      MetalIconFactory.getHorizontalSliderThumbIcon(),
      "Slider.majorTickLength", new Integer(6),
      "Slider.shadow", getControlShadow(),
      "Slider.trackWidth", new Integer(7),
      "Slider.verticalThumbIcon", 
      MetalIconFactory.getVerticalSliderThumbIcon(),

      "Spinner.background", getControl(),
      "Spinner.font", new FontUIResource("Dialog", Font.BOLD, 12),
      "Spinner.foreground", getControl(),

      "SplitPane.background", getControl(),
      "SplitPane.darkShadow", getControlDarkShadow(),
      "SplitPane.dividerFocusColor", getPrimaryControl(),
      "SplitPane.highlight", getControlHighlight(),
      "SplitPane.shadow", getControlShadow(),

      "SplitPaneDivider.draggingColor", Color.DARK_GRAY,

      "TabbedPane.background", getControlShadow(),
      "TabbedPane.darkShadow", getControlDarkShadow(),
      "TabbedPane.focus", getPrimaryControlDarkShadow(),
      "TabbedPane.font", new FontUIResource("Dialog", Font.BOLD, 12),
      "TabbedPane.foreground", getControlTextColor(),
      "TabbedPane.highlight", getControlHighlight(),
      "TabbedPane.light", getControl(),
      "TabbedPane.selected", getControl(),
      "TabbedPane.selectHighlight", getControlHighlight(),
      "TabbedPane.selectedTabPadInsets", new InsetsUIResource(2, 2, 2, 1),
      "TabbedPane.shadow", getControlShadow(),
      "TabbedPane.tabAreaBackground", getControl(),
      "TabbedPane.tabAreaInsets", new InsetsUIResource(4, 2, 0, 6),
      "TabbedPane.tabInsets", new InsetsUIResource(0, 9, 1, 9),
      
      "Table.background", getWindowBackground(),
      "Table.focusCellBackground", getWindowBackground(),
      "Table.focusCellForeground", getControlTextColor(),
      "Table.foreground", getControlTextColor(),
      "Table.focusCellHighlightBorder",
      new BorderUIResource.LineBorderUIResource(getControlShadow()),
      "Table.focusCellBackground", getWindowBackground(),
      "Table.gridColor", getControlDarkShadow(),
      "Table.selectionBackground", new ColorUIResource(204, 204, 255),
      "Table.selectionForeground", new ColorUIResource(0, 0, 0),

      "TableHeader.background", getControl(),
      "TableHeader.cellBorder", new MetalBorders.TableHeaderBorder(),
      "TableHeader.foreground", getControlTextColor(),

      "TextArea.background", getWindowBackground(),
      "TextArea.caretForeground", getUserTextColor(),
      "TextArea.font", new FontUIResource("Dialog", Font.PLAIN, 12),
      "TextArea.foreground", getUserTextColor(),
      "TextArea.inactiveForeground", getInactiveSystemTextColor(),
      "TextArea.selectionBackground", getTextHighlightColor(),
      "TextArea.selectionForeground", getHighlightedTextColor(),

      "TextField.background", getWindowBackground(),
      "TextField.border",
      new BorderUIResource(MetalBorders.getTextFieldBorder()),
      "TextField.caretForeground", getUserTextColor(),
      "TextField.darkShadow", getControlDarkShadow(),
      "TextField.font", new FontUIResource("Dialog", Font.PLAIN, 12),
      "TextField.foreground", getUserTextColor(),
      "TextField.highlight", getControlHighlight(),
      "TextField.inactiveBackground", getControl(),
      "TextField.inactiveForeground", getInactiveSystemTextColor(),
      "TextField.light", getControlHighlight(),
      "TextField.selectionBackground", getTextHighlightColor(),
      "TextField.selectionForeground", getHighlightedTextColor(),
      "TextField.shadow", getControlShadow(),
     
      "TextPane.background", getWindowBackground(),
      "TextPane.caretForeground", getUserTextColor(),
      "TextPane.font", new FontUIResource("Dialog", Font.PLAIN, 12),
      "TextPane.foreground", getUserTextColor(),
      "TextPane.inactiveForeground", getInactiveSystemTextColor(),
      "TextPane.selectionBackground", getTextHighlightColor(),
      "TextPane.selectionForeground", getHighlightedTextColor(),

      "TitledBorder.font", new FontUIResource("Dialog", Font.BOLD, 12),
      "TitledBorder.titleColor", getSystemTextColor(),

      "ToggleButton.background", getControl(),
      "ToggleButton.border", MetalBorders.getToggleButtonBorder(),
      "ToggleButton.darkShadow", getControlDarkShadow(),
      "ToggleButton.disabledText", getInactiveControlTextColor(),
      "ToggleButton.focus", getFocusColor(),
      "ToggleButton.font", getControlTextFont(),
      "ToggleButton.foreground", getControlTextColor(),
      "ToggleButton.highlight", getControlHighlight(),
      "ToggleButton.light", getControlHighlight(),
      "ToggleButton.margin", new InsetsUIResource(2, 14, 2, 14),
      "ToggleButton.select", getControlShadow(),
      "ToggleButton.shadow", getControlShadow(),

      "ToolBar.background", getMenuBackground(),
      "ToolBar.darkShadow", getControlDarkShadow(),
      "ToolBar.dockingBackground", getMenuBackground(),
      "ToolBar.dockingForeground", getPrimaryControlDarkShadow(),
      "ToolBar.floatingBackground", getMenuBackground(),
      "ToolBar.floatingForeground", getPrimaryControl(),
      "ToolBar.font", new FontUIResource("Dialog", Font.BOLD, 12),
      "ToolBar.foreground", getMenuForeground(),
      "ToolBar.highlight", getControlHighlight(),
      "ToolBar.light", getControlHighlight(),
      "ToolBar.shadow", getControlShadow(),
      "ToolBar.border", new MetalBorders.ToolBarBorder(),

      "ToolTip.background", getPrimaryControl(),
      "ToolTip.backgroundInactive", getControl(),
      "ToolTip.border", new BorderUIResource.LineBorderUIResource(getPrimaryControlDarkShadow(), 1),
      "ToolTip.borderInactive", new BorderUIResource.LineBorderUIResource(getControlDarkShadow(), 1),
      "ToolTip.font", new FontUIResource("Dialog", Font.PLAIN, 12),
      "ToolTip.foreground", getPrimaryControlInfo(),
      "ToolTip.foregroundInactive", getControlDarkShadow(),

      "Tree.background", getWindowBackground(),
      "Tree.closedIcon", MetalIconFactory.getTreeFolderIcon(),
      "Tree.collapsedIcon", MetalIconFactory.getTreeControlIcon(true),
      "Tree.expandedIcon", MetalIconFactory.getTreeControlIcon(false),
      "Tree.font", new FontUIResource("Dialog", Font.PLAIN, 12),
      "Tree.foreground", getUserTextColor(),
      "Tree.hash", getPrimaryControl(),
      "Tree.leafIcon", MetalIconFactory.getTreeLeafIcon(),
      "Tree.leftChildIndent", new Integer(7),
      "Tree.line", getPrimaryControl(),
      "Tree.openIcon", MetalIconFactory.getTreeFolderIcon(),
      "Tree.rightChildIndent", new Integer(13),
      "Tree.rowHeight", new Integer(0),
      "Tree.scrollsOnExpand", Boolean.TRUE,
      "Tree.selectionBackground", getTextHighlightColor(),
      "Tree.selectionBorder", new BorderUIResource.LineBorderUIResource(new Color(102, 102, 153)),
      "Tree.selectionBorderColor", getFocusColor(),
      "Tree.selectionForeground", getHighlightedTextColor(),
      "Tree.textBackground", getWindowBackground(),
      "Tree.textForeground", getUserTextColor(),

      "Viewport.background", getControl(),
      "Viewport.foreground", getUserTextColor()
    };
    defaults.putDefaults(myDefaults);
  }

  /**
   * Initializes the system color defaults.
   *
   * In particular this sets the following keys:
   *
   * <table>
   * <tr>
   * <th>Key</th><th>Value</th><th>Description</th>
   * </tr><tr>
   * <td>control</td><td>0xcccccc</td><td>The default color for components</td>
   * </tr>
   * </table>
   */
  protected void initSystemColorDefaults(UIDefaults defaults)
  {
    super.initSystemColorDefaults(defaults);
    Object[] uiDefaults;
    uiDefaults = new Object[] {
      "control", new ColorUIResource(getControl()),
      "desktop", new ColorUIResource(getDesktopColor())
    };
    defaults.putDefaults(uiDefaults);
  }

  /**
   * Returns the current theme setting for the Metal L&amp;F.
   *
   * @return the current theme setting for the Metal L&amp;F
   */
  public static MetalTheme getCurrentTheme()
  {
    return theme;
  }
}
