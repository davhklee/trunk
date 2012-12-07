
import bb.cascades 1.0
import bb.cascades.advertisement 1.0

NavigationPane {
    
    	id: nav
    	
    	Menu.definition: MenuDefinition {
    	    helpAction: HelpActionItem {
    	        onTriggered: {
    	            nav.push(helppage);
    	        }
    	    }
    	    settingsAction: SettingsActionItem {
    	        onTriggered: {
                    mytogglebutton.checked = app.value;
    	            nav.push(settingspage);
    	        }
    	    }
            actions: [ ActionItem {
                title: qsTr("About")
                imageSource: "asset:///images/about.png"
                onTriggered: {
                    nav.push(aboutpage);
                }
            } ]
    	}

        Page {
            
            content: Container {
                
                layout: DockLayout {}

                ImageView {
                    verticalAlignment: VerticalAlignment.Fill
                    horizontalAlignment: HorizontalAlignment.Fill
                    imageSource: "asset:///images/background.png"
                }
    
                Container {
                    
                    layout: StackLayout {
                        orientation: LayoutOrientation.TopToBottom
                    }
                    
                    leftPadding: 10.0
                    topPadding: 10.0
                    rightPadding: 10.0
                    bottomPadding: 10.0
                    
                    Container {
                        
                        layout: StackLayout {
                            orientation: LayoutOrientation.LeftToRight
                        }
                        
                        TextField {
                            id: mysearchstr
                            text: qsTr("Please enter song name")
                            horizontalAlignment: HorizontalAlignment.Left
                            preferredHeight: 10.0
                            minHeight: 10.0
                            maxHeight: 10.0
                            preferredWidth: DisplayInfo.width * 0.8
                            minWidth: DisplayInfo.width * 0.8
                            maxWidth: DisplayInfo.width * 0.8
                            input {
                                onSubmitted: {
                                    if (mysearchstr.text != "") {
                                    	app.startXML(mysearchstr.text, 0);
                                    	myindicator.start();
                                    }
                                }
                            }
                            onTextChanging: {
                                if (mysearchstr.text != "") {
                                    mysearchbutton.enabled = true
                                } else {
                                    mysearchbutton.enabled = false
                                }
                            }
                        }
                        
                        Button {
                            id: mysearchbutton
                            imageSource: "asset:///images/find.png"
                            horizontalAlignment: HorizontalAlignment.Right
                            preferredHeight: 10.0
                            minHeight: 10.0
                            maxHeight: 10.0
                            onClicked: {
                                app.startXML(mysearchstr.text, 0);
                                myindicator.start();
                            }
                            function on_received(val) {
                                mylistview.dataModel.source = qdpm.nonefile;
                                mylistview.dataModel.source = qdpm.searchfile;
                                myindicator.stop();
                            }
                        }

                    }
                    
                    Container {
                        
                        layout: DockLayout {}
                        
                        horizontalAlignment: HorizontalAlignment.Fill
                        
                        ActivityIndicator {
                            id: myindicator
                            preferredHeight: 10.0
                            minHeight: 10.0
                            maxHeight: 10.0
                            horizontalAlignment: HorizontalAlignment.Right
                        }
                        
                        Banner {
                            horizontalAlignment: HorizontalAlignment.Center
                            zoneId: 223229
                            //zoneId: 117145
                            refreshRate: 60
                            preferredWidth: 320
                            preferredHeight: 50
                            transitionsEnabled: true
                            placeHolderURL: "asset:///images/ad.png"
                            backgroundColor: Color.Black
                            borderColor: Color.White
                            borderWidth: 2
                        }

                    }

                    Container {
                        
                        layout: StackLayout {
                            orientation: LayoutOrientation.TopToBottom
                        }
                        
                        ListView {
                            id: mylistview
                            verticalAlignment: VerticalAlignment.Top
                            dataModel: XmlDataModel {
                                source: qdpm.searchfile
                            }
                            listItemComponents: [
                                ListItemComponent {
                                    type: "loading"
                                    Container {
                                        layout: StackLayout {
                                            orientation: LayoutOrientation.LeftToRight
                                        }
                                        Header {
                                            title: qsTr("Contacting server")
                                        }
                                    }
                                },
                                ListItemComponent {
                                    type: "failed"
                                    Container {
                                        layout: StackLayout {
                                            orientation: LayoutOrientation.LeftToRight
                                        }
                                        Header {
                                            title: qsTr("No result found")
                                        }
                                    }
                                },
                                ListItemComponent {
                                    type: "notfound"
                                    Container {
                                        layout: StackLayout {
                                            orientation: LayoutOrientation.LeftToRight
                                        }
                                        Header {
                                            title: qsTr("Search history cleared")
                                        }
                                    }
                                },
                                ListItemComponent {
                                    type: "record"
                                    Container {
                                        layout: StackLayout {
                                            orientation: LayoutOrientation.LeftToRight
                                        }
                                        ImageView {
                                            maxWidth: 50.0
                                            maxHeight: 50.0
                                            minHeight: 50.0
                                            minWidth: 50.0
                                            preferredWidth: 50.0
                                            preferredHeight: 50.0
                                            imageSource: ListItemData.icon
                                            //imageSource: "asset:///images/audio.png"
                                        }
                                        StandardListItem {
                                            title: ListItemData.title
                                            description: ListItemData.artist
                                            status: qsTr("Online");
                                        }
                                    }
                                }
                            ]
                            onTriggered: {
                                var selItem = dataModel.data(indexPath);
                                var listening = qsTr("Listening on Twistar: ");
                                listening += selItem.title;
                                if (app.value > 0) {
                                	app.updatePersonalMessage(listening);
                                }
                                app.launchBrowser(selItem.url);
                            }
                        }
                        
                    } // inner container
                    
                } // middle container
                
            } // outer container
            
            actions: [
                ActionItem {
                    title: qsTr("Billboard")
                    imageSource: "asset:///images/billboard.png"
                    onTriggered: {
                        app.startXML("", 0);
                        myindicator.start();
                    }
                    ActionBar.placement: ActionBarPlacement.OnBar
                },
                ActionItem {
                    title: qsTr("Top")
                imageSource: "asset:///images/top.png"
                    onTriggered: {
                        mylistview.scrollToPosition(0, 0);
                    }
                    ActionBar.placement: ActionBarPlacement.OnBar
                },
                ActionItem {
                    title: qsTr("Bottom")
                imageSource: "asset:///images/bottom.png"
                    onTriggered: {
                        mylistview.scrollToPosition(1, 0);
                    }
                    ActionBar.placement: ActionBarPlacement.OnBar
                },
                ActionItem {
                    title: qsTr("Invite BBM Download")
                    imageSource: "asset:///images/bbm.png"
                    onTriggered: {
                        app.inviteUserToDownloadViaBBM();
                    }
                    ActionBar.placement: ActionBarPlacement.InOverflow
                },
                InvokeActionItem {
                    ActionBar.placement: ActionBarPlacement.InOverflow
                    query.invokeTargetId: "sys.pim.uib.email.hybridcomposer"
                    query.invokeActionId: "bb.action.SENDEMAIL"
                    query.mimeType: ""
                    query.uri: qsTr("mailto:automechtic@hotmail.com?subject=Twistar%20Technical%20Support")
                    query.invokerIncluded: true
                    title: qsTr("Contact Us")
                },
                DeleteActionItem {
                    title: qsTr("Clear History")
                    onTriggered: {
                        app.displayXML("<root><notfound/></root>");
                    }
                    ActionBar.placement: ActionBarPlacement.InOverflow
                }
            ]
            
            onCreationCompleted: {

                app.complete.connect(mysearchbutton.on_received);
                app.startXML("", 0);
                myindicator.start();
            
            }
            
        } // page
        
    attachedObjects: [

        Page {
            id: aboutpage
            content: Container {
                layout: StackLayout {
                    orientation: LayoutOrientation.TopToBottom
                }
                topPadding: 10.0
                leftPadding: 10.0
                rightPadding: 10.0
                bottomPadding: 10.0
                Label {
                    text: qsTr("Twistar v1.0.0.3")
                }
                Label {
                    text: qsTr("This software is developed, tested and distributed by developer Automechtic.  Automechtic reserves the rights to main application icon, source code and search algorithm contained herein.  2013 All rights reserved.  This software uses a unique search algorithm to locate digital contents on the world wide web.  It does not imply ownership of contents it links to, neither does it own, facilitate or endorse their contents.  For application support, please contact us.")
                    multiline: true
                    autoSize.maxLineCount: 40
                }
            }
        },
        Page {
          id: helppage
          titleBar: TitleBar {
              title: qsTr("Help");
          }
          content: Container {
              layout: StackLayout {
                  orientation: LayoutOrientation.TopToBottom
              }
              leftPadding: 10.0
              topPadding: 10.0
              rightPadding: 10.0
              bottomPadding: 10.0
              Label {
                  text: qsTr("Twistar uses a unique search algorithm to locate songs available on the world wide web.")
                  multiline: true
                  autoSize.maxLineCount: 10
              }
              Label {
                  text: qsTr("To start searching for your favorite songs, simply enter song name in the text box on top and press ENTER on your keyboard or press the magnifying glass button to the right.")
                  multiline: true
                  autoSize.maxLineCount: 10
              }
              Label {
                  text: qsTr("To help you navigate through the song list on phones with physical keyboard, use 't' button to move to top of the list, 'b' to bottom of the list, 'p' to move back by one song and 'n' to move forward by one.")
                  multiline: true
                  autoSize.maxLineCount: 10
              }
          }
        },
        Page {
            id: settingspage
            titleBar: TitleBar {
                title: qsTr("Settings")
                visibility: ChromeVisibility.Visible
                dismissAction: ActionItem {
                    title: qsTr("Cancel")
                    onTriggered: {
                        nav.pop();
                    }
                }
                acceptAction: ActionItem {
                    title: qsTr("Save")
                    onTriggered: {
                        app.value = mytogglebutton.checked;
                        nav.pop();
                    }
                }
            }
            content: Container {
                layout: DockLayout {}
                leftPadding: 10.0
                topPadding: 10.0
                rightPadding: 10.0
                bottomPadding: 10.0
                Label {
                    text: qsTr("Display song on BBM status?")
                    horizontalAlignment: HorizontalAlignment.Left
                }
                ToggleButton {
                    id: mytogglebutton
                    horizontalAlignment: HorizontalAlignment.Right
                }
            }
        }
    ]

} // navigation
