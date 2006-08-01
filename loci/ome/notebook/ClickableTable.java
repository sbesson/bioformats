/*
* ClickableTable.java
*
*   a class that makes tables you can right click to
* add or subtract duplicate tables and get information
* on the attributes being manipulated
*/

/*
* Written by:  Christopher Peterson  <crpeterson2@wisc.edu>
*/


package loci.ome.notebook;

import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Color;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.openmicroscopy.xml.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Vector;

public class ClickableTable extends JTable 
  implements MouseListener, ActionListener, ListSelectionListener {

//stores the TablePanel this table is associated with
  protected MetadataPane.TablePanel tp;
  
//is the current popup menu at any given rightclick
  protected JPopupMenu jPop;
  
//the current row being clicked on at any point  
  private int thisRow;
  
//the name of the attribute in the row being clicked on currently
  private String attrName;
  
//tells at any given point if the TablePanel being added or deleted
//is a "duplicate" , e.g. if there is more than one element with its
//same tagname on a given level of the node tree
  private boolean isDuplicate;

  // -- ClickableTable Constructors --

  public ClickableTable(TableModel model, MetadataPane.TablePanel tablePanel) {
    super(model);
    
    addMouseListener(this);

//initialize various fields
    tp = tablePanel;
    jPop = new JPopupMenu();
    thisRow = -1;
    attrName = null;

//setup a selectionlistener on this table so that if any row is selected it
    //is immediately deselected (work-around 
    //for multiple selection irritations)    
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    //Ask to be notified of selection changes.
    ListSelectionModel rowSM = getSelectionModel();
    rowSM.addListSelectionListener(new ListSelectionListener() {
    public void valueChanged(ListSelectionEvent e) {
	    //Ignore extra messages.
	    if (e.getValueIsAdjusting()) return;
	
	    ListSelectionModel lsm =
	        (ListSelectionModel)e.getSource();
	    if (lsm.isSelectionEmpty()) {
	        //no rows are selected
	    } 
	    else {
	        lsm.clearSelection();
	        //selectedRow is selected
	    }
    }
});

  }
  
  // -- Static ClickableTable API Methods --

  //tests if the given tagname should be 
  //placed under a CustomAttributesNode  
  public static boolean isInCustom(String tagName) {
    return MetadataPane.isInCustom(tagName);
  }
  
  //tests if this word should have an "a" or an "an"
  //before it. char c is the first character of the word. 
  public static boolean usesAn(char c) {
    boolean result = false;
    switch(c) {
      case 'a':
      case 'A':
      case 'e':
      case 'E':
      case 'i':
      case 'I':
      case 'o':
      case 'O':
      case 'h':
      case 'H':
        result = true;
        break;
      default:
        result = false;
        break;
    }
    return result;
  }
  
  // -- MouseListener API Methods --
  
  public void mousePressed(MouseEvent e) {
    //test if button 2 or 3 are pressed
    if (e.getButton() == MouseEvent.BUTTON3 
      || e.getButton() == MouseEvent.BUTTON2) 
    {
      //nifty table method, sees which row the pointer is in
      thisRow = rowAtPoint(e.getPoint());
      //given the row, get the appropriate attribute's name
      attrName = (String) getModel().getValueAt(thisRow,0);
      
      //setup the popup menu based on this information
      jPop = new JPopupMenu("Add/Remove " + attrName + " Attribute:");
      JMenuItem infoItem = null;
      if( usesAn(attrName.charAt(0)) ) infoItem = 
        new JMenuItem("What is an " + attrName + "?");
      else infoItem = new JMenuItem("What is a " + attrName + "?");
      
      //strip away the "(x)" at the end of the tablepanel's name so
      //it makes sense in the menu, e.g. "Add another Project (2)"
      //is inaccurate, while "Add another Project" is what we want
      String realBigName = tp.name;
      isDuplicate = false;
      if(realBigName.endsWith(")") ) {
        isDuplicate = true;
        realBigName = realBigName.substring(0,realBigName.length()-4);
      }
      
      //setup the various menuitems in the popup menu
      JMenuItem addItem = new JMenuItem("Add another " + realBigName);
      JMenuItem bigRemItem = new JMenuItem("Delete this " + realBigName);
      JMenuItem remItem = new JMenuItem("Delete this " + attrName);
      infoItem.addActionListener(this);
      infoItem.setActionCommand("help");
      addItem.addActionListener(this);
      addItem.setActionCommand("bigAdd");
      bigRemItem.addActionListener(this);
      bigRemItem.setActionCommand("bigRem");
      remItem.addActionListener(this);
      remItem.setActionCommand("delete");
      
      //add the menuitems to the popup menu, add logical separators
      jPop.add(infoItem);
      JSeparator sep = new JSeparator();
      jPop.add(sep);
      jPop.add(remItem);
      JSeparator sep2 = new JSeparator();
      jPop.add(sep2);
      jPop.add(addItem);
      jPop.add(bigRemItem);
      jPop.show(this, e.getX(), e.getY());
    }
  }
  
  //abstract methods we must override but have no use for
  public void mouseReleased(MouseEvent e) {}
  public void mouseClicked(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}
  
  // -- ActionLister API Methods --
  
  //handles the actions caused by selection in the popup menu
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    //create a HelpFrame if user requests help on an attribute
    if ("help".equals(cmd)) {
      HelpFrame helpWin = new HelpFrame();
    }
    //handle deleting of a single attribute's value in the table
    if ("delete".equals(cmd)) {
      //get a list of all attributes in this TablePanel
      Vector attrVector = DOMUtil.getChildElements("OMEAttribute", tp.el);
      Element thisAttr = null;
      //test if attrName is a "Name" or "XMLName" attribute, find the element
      //that matches attrName 
      for (int i = 0;i<attrVector.size();i++) {
        Element temp = (Element) attrVector.get(i);
        if (temp.hasAttribute("Name")) {
          if (attrName.equals(temp.getAttribute("Name")) ) thisAttr = temp;
        }
        else if (temp.hasAttribute("XMLName") && !temp.hasAttribute("Name") ) {
          if (attrName.equals(temp.getAttribute("XMLName")) ) thisAttr = temp;
        }
      }
      
      //set nodetree to reflect a blank attribute here, also set table blank
      tp.oNode.getDOMElement().removeAttribute(thisAttr.getAttribute("XMLName"));
      getModel().setValueAt("", thisRow, 1);
    }
    //this signifies that the user wants to add another "clone" TablePanel
    if ("bigAdd".equals(cmd)) {
      //get the tagname of the element associated with this tablepanel
      String thisTagName =tp.oNode.getDOMElement().getTagName();
      
      //test if the tablepanel in question is actually a tab, e.g. the only
      //ancestor nodes are CustomAttributesNode and/or OMENode
      if (tp.isTopLevel) {        
        //test if we need to deal with CustomAttributesNodes using the
        //isInCustom(String tagName) static method
        MetadataPane.makeNode(thisTagName,tp.tPanel.ome);

				//tell the tablepanel to tell the MetadataPane to redo its GUI based on
				//the new node tree structure           
        tp.callReRender();
      }
      //if tablepanel doesn't represent a "top-level" element
      else {
        //test if we need to deal with CustomAttributesNodes
        MetadataPane.makeNode(thisTagName,tp.tPanel.oNode);
        
        //tell the tablepanel to tell the MetadataPane to redo its GUI based on
				//the new node tree structure    
        tp.callReRender();
      }
    }
    //signifies user wishes to delete an entire tablepanel.
    //N.B. : if there is only one instance of the tablepanel in question,
    //it will be deleted then recreated blank in order to comply with the
    //template
    if ("bigRem".equals(cmd)) {
      //test if we're dealing with a "top-level" element
      if (tp.isTopLevel) {
        String thisTagName =tp.oNode.getDOMElement().getTagName();
        Element parentEle = null;
        if (!isInCustom(thisTagName)) {
          parentEle = tp.tPanel.ome.getDOMElement();
          //remove the node in question from its parent
          parentEle.removeChild((Node) tp.oNode.getDOMElement());
        }
        else {
          OMEXMLNode realParent = tp.tPanel.ome.getChild("CustomAttributes");
          parentEle = realParent.getDOMElement();
          //remove the node in question from its (CustomAttributes) parent
          parentEle.removeChild((Node) tp.oNode.getDOMElement());

	        NodeList caChildren = parentEle.getChildNodes();
		      if ( caChildren != null) {
		        if ( caChildren.getLength() == 0) {
		          tp.tPanel.ome.getDOMElement().removeChild( (Node) parentEle);
		        }
		      }
		      else tp.tPanel.oNode.getDOMElement().removeChild( (Node) parentEle);            
        }
        
        //tell the tablepanel to tell the MetadataPane to redo its GUI based on
				//the new node tree structure
        tp.callReRender();
      }
      //if not a "top-level" element, do this
      else {
        String thisTagName =tp.oNode.getDOMElement().getTagName();
        if (!isInCustom(thisTagName)) {
          Element parentEle = tp.tPanel.oNode.getDOMElement();
          parentEle.removeChild((Node) tp.oNode.getDOMElement());
        }
        else {
          OMEXMLNode realParent = tp.tPanel.oNode.getChild("CustomAttributes");
          Element parentEle = realParent.getDOMElement(); 
          parentEle.removeChild((Node) tp.oNode.getDOMElement());
          
          NodeList caChildren = parentEle.getChildNodes();
		      if ( caChildren != null) {
		        if ( caChildren.getLength() == 0) {
		          tp.tPanel.oNode.getDOMElement().removeChild( (Node) parentEle);
		        }
		      }
		      else tp.tPanel.oNode.getDOMElement().removeChild( (Node) parentEle);
        }
        
        //tell the tablepanel to tell the MetadataPane to redo its GUI based on
				//the new node tree structure
        tp.callReRender();
      }
    }
  }

  // -- Helper Classes --
  
  public class HelpFrame extends JFrame {
    //the only constructor
    public HelpFrame() {
      //set up the frame itself
      super("Help! - " + tp.name);
      setLocation(200,200);
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      
      //make a content panel to display stuff on, set its size
      JPanel contentPanel = new JPanel();
      Dimension dim = new Dimension(300,125);
      contentPanel.setPreferredSize(dim);
      contentPanel.setLayout(new BorderLayout());
      setContentPane(contentPanel);
      contentPanel.setBackground(new Color(0,0,50));

			//create a label corresponding to the attribute in question
      JLabel titleLabel = new JLabel(" " + attrName + ":");
      Font thisFont = titleLabel.getFont();
      Font newFont = new Font(thisFont.getFontName(),Font.BOLD,18);
      titleLabel.setFont(newFont);
      contentPanel.add(titleLabel, BorderLayout.NORTH);
      titleLabel.setForeground(new Color(255,255,255));

      //set default help text
      String desc = "      No description available for " + attrName + ".";
      //cruise the template's node tree to get the appropriate 
      //OMEAttribute's "Description" attribute
      Vector attrVector = DOMUtil.getChildElements("OMEAttribute", tp.el);
      Element thisAttr = null;
      for (int i = 0;i<attrVector.size();i++) {
        Element temp = (Element) attrVector.get(i);
        if (temp.hasAttribute("Name")) {
          if (attrName.equals(temp.getAttribute("Name")) ) thisAttr = temp;
        }
        else if (temp.hasAttribute("XMLName") && ! temp.hasAttribute("Name") ) {
          if (attrName.equals(temp.getAttribute("XMLName")) ) thisAttr = temp;
        }
      }
      if (thisAttr != null && thisAttr.hasAttribute("Description")) 
        desc = "      " + thisAttr.getAttribute("Description");

      //make a textarea to hold the description found
      JTextArea descArea = new JTextArea(desc);
      descArea.setEditable(false);
      descArea.setLineWrap(true);
      descArea.setWrapStyleWord(true);
      JScrollPane jScr = new JScrollPane(descArea);
      contentPanel.add(jScr, BorderLayout.CENTER);      
      
      //make the frame the right size and visible
      pack();
      setVisible(true);
    }
  }
}

