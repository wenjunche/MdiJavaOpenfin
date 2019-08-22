package com.openfin.desktop.demo;// Fig. 22.11: DesktopFrame.java
// Demonstrating JDesktopPane.
import com.openfin.desktop.*;
import com.openfin.desktop.Window;
import com.sun.jna.Native;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyVetoException;
import java.lang.System;
import java.util.UUID;

/**
 * Created by Praveen Lowanshi on 2/24/2019.
 */
public class DesktopFrame extends JFrame 
{
   private JDesktopPane theDesktop;
   private String openfin_app_url = "https://www.google.com";

   protected DesktopConnection desktopConnection;

   public DesktopFrame()
   {
      super( "Using a JDesktopPane" );
      try {
         this.desktopConnection = new DesktopConnection(appUuid);
      } catch (DesktopException desktopError) {
         desktopError.printStackTrace();
      }

      JMenu menu, submenu;
      JMenuItem i1, i2, i3, i4, i5;

      startOpenFinRuntime();
      JMenuBar bar = new JMenuBar(); // create menu bar
      menu=new JMenu("Menu");
      submenu=new JMenu("Sub Menu");
      i1=new JMenuItem("Item 1");
      i2=new JMenuItem("Item 2");
      i3=new JMenuItem("Item 3");
      i4=new JMenuItem("Item 4");
      i5=new JMenuItem("Item 5");
      menu.add(i1); menu.add(i2); menu.add(i3);
      submenu.add(i4); submenu.add(i5);
      menu.add(submenu);
      bar.add(menu);
      setJMenuBar( bar ); // set menu bar for this application

      embedCanvas = new java.awt.Canvas();
      embedCanvas.setFocusable(true);
      
      //create a hidden frame to hold the openfin window when it's iconified.
      final JFrame hiddenFrame = new JFrame();
      hiddenFrame.setPreferredSize(new Dimension(640, 480));
      hiddenFrame.pack();
      hiddenFrame.setVisible(false);
      
		JInternalFrame frame = new JInternalFrame("Internal Frame", true, true, true, true) {
		
			private void superSetIcon(boolean b) throws PropertyVetoException {
				super.setIcon(b);
			}
		
			@Override
			public void setIcon(boolean b) throws PropertyVetoException {
				if (b) {
					// when iconified, JInternalFrame is removed from the container
					// it causes the renderer of openfin window to be destroyed
					// workaround: to embed openfin window to the hidden frame.
					long hiddenFrameHWndId = Native.getComponentID(hiddenFrame);
//					startupHtml5app.getWindow().embedInto(hiddenFrameHWndId, 640, 480, new AckListener() {
//						@Override
//						public void onSuccess(Ack ack) {
//							try {
//								superSetIcon(b);
//							}
//							catch (PropertyVetoException e) {
//								e.printStackTrace();
//							}
//						}
//						@Override
//						public void onError(Ack ack) {
//						}
//					});
                    superSetIcon(b);
				} else {
					superSetIcon(b);
				}
			}
		};

		frame.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameDeiconified(InternalFrameEvent e) {
				//when internal frame is restored, re-embed openfin window back to canvas
				long canvasHWndId = Native.getComponentID(embedCanvas);
                launchHTMLApps();
//				startupHtml5app.getWindow().embedInto(canvasHWndId, embedCanvas.getWidth(), embedCanvas.getHeight(), new AckListener() {
//					@Override
//					public void onSuccess(Ack ack) {
//					}
//					@Override
//					public void onError(Ack ack) {
//					}
//				});
			}
		});
		

      frame.add( embedCanvas, BorderLayout.CENTER );
      frame.setSize(1000,1000);
      theDesktop = new JDesktopPane(); // create desktop pane
      theDesktop.add( frame ); // attach internal frame
      frame.setVisible( true ); // show internal frame

		//when mixing lightweight and heavyweight component
		frame.addComponentListener(new ComponentListener() {
			private void revalidateWorkaround() {
				theDesktop.revalidate();
				DesktopFrame.this.validate();
			}

			@Override
			public void componentResized(ComponentEvent e) {
				revalidateWorkaround();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				revalidateWorkaround();
			}

			@Override
			public void componentShown(ComponentEvent e) {
				revalidateWorkaround();
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				revalidateWorkaround();
			}
		});
      
      
      
      embedCanvas.addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent event) {
            super.componentResized(event);
            Dimension newSize = event.getComponent().getSize();
            try {
               if (startupHtml5app != null) {
                  startupHtml5app.getWindow().embedComponentSizeChange((int)newSize.getWidth(), (int)newSize.getHeight());
               }
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      });


       add( theDesktop ); // add desktop pane to frame
    
   } // end constructor DesktopFrame

   private void launchHTMLApps() {
      // launch 5 instances of same example app
      int width = 300, height=200;
      int gap = 50;  // gap between windows at initial positions

         try {
            String uuid = UUID.randomUUID().toString();
            ApplicationOptions options = new ApplicationOptions(uuid, uuid, openfin_app_url);
            WindowOptions mainWindowOptions = new WindowOptions();
            mainWindowOptions.setAutoShow(true);
            mainWindowOptions.setFrame(false);
            mainWindowOptions.setResizable(false);
            mainWindowOptions.setDefaultHeight(height);
            mainWindowOptions.setDefaultTop(50);
            mainWindowOptions.setDefaultWidth(width);
            mainWindowOptions.setShowTaskbarIcon(true);
            mainWindowOptions.setSaveWindowState(false);  // set to false so all windows start at same initial positions for each run
            options.setMainWindowOptions(mainWindowOptions);
            launchHTMLApp(options, new AckListener() {
               @Override
               public void onSuccess(Ack ack) {
                  Application app = (Application) ack.getSource();
                  try {
                     Thread.sleep(1000);
                     embedStartupApp(app);
                  } catch (Exception ex) {
                     ex.printStackTrace();
                  }
               }
               @Override
               public void onError(Ack ack) {
                   System.out.println(String.format("Error launching %s %s", options.getUUID(), ack.getReason()));
               }
            });
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("Error launching app");
         }
      }

   private void launchHTMLApp(ApplicationOptions options, AckListener ackListener) throws Exception {
      //logger.debug(String.format("Launching %s", options.getUUID()));
      DemoUtils.runApplication(options, this.desktopConnection, ackListener);
   }
   private void startOpenFinRuntime() {
      try {
         DesktopStateListener listener = new DesktopStateListener() {
            @Override
            public void onReady() {
               onRuntimeReady();
            }

            private void onRuntimeReady() {
              // addMenu.setEnabled(true);
               launchHTMLApps();

            }

            @Override
            public void onClose(String reason) {
               // updateMessagePanel(String.format("Connection closed %s", error));
            }
            @Override
            public void onError(String reason) {
               //updateMessagePanel("Connection failed: " + reason);
            }
            @Override
            public void onMessage(String message) {
            }
            @Override
            public void onOutgoingMessage(String message) {
            }
         };
         RuntimeConfiguration configuration = new RuntimeConfiguration();
         configuration.setDevToolsPort(9090);
         configuration.setAdditionalRuntimeArguments(" --v=1 --inspect "); // enable additional logging from Runtime
         String desktopVersion = java.lang.System.getProperty("com.openfin.demo.version");
         if (desktopVersion == null) {
            desktopVersion = "stable";
         }
         configuration.setRuntimeVersion(desktopVersion);
         desktopConnection.connect(configuration, listener, 60);

      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   private Application startupHtml5app;
   protected String appUuid = "JavaEmbedding";
   protected String startupUuid = "OpenFinHelloWorld";
   protected java.awt.Canvas embedCanvas;
   protected Long previousPrarentHwndId;
   private void embedStartupApp(Application app) {
      try {
         if (app == null) {
            app = Application.wrap(this.startupUuid, this.desktopConnection);
         }
         startupHtml5app =app;
         Window html5Wnd = app.getWindow();
         long parentHWndId = Native.getComponentID(this.embedCanvas);
         System.out.println("Canvas HWND " + Long.toHexString(parentHWndId));
         html5Wnd.embedInto(parentHWndId, this.embedCanvas.getWidth(), this.embedCanvas.getHeight(), new AckListener() {
            @Override
            public void onSuccess(Ack ack) {
               if (ack.isSuccessful()) {
                  previousPrarentHwndId = ack.getJsonObject().getLong("hWndPreviousParent");
               } else {
                  java.lang.System.out.println("embedding failed: " + ack.getJsonObject().toString());
               }
            }
            @Override
            public void onError(Ack ack) {
            }
         });
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}