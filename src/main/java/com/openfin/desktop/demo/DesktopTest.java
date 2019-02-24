package com.openfin.desktop.demo;// Fig. 22.12: DesktopTest.java
// Demonstrating JDesktopPane.
import com.openfin.desktop.demo.DesktopFrame;

import javax.swing.JFrame;

/**
 * Created by Praveen Lowanshi on 2/24/2019.
 */
public class DesktopTest 
{
   public static void main( String args[] )
   { 
      DesktopFrame desktopFrame = new DesktopFrame();
      desktopFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      desktopFrame.setSize( 1000, 1000 ); // set frame size
      desktopFrame.setVisible( true ); // display frame
   } // end main
} // end class DesktopTest


