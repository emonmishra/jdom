/*-- 

 $Id: JDOMResult.java,v 1.18 2003/05/29 02:52:05 jhunter Exp $

 Copyright (C) 2001 Jason Hunter & Brett McLaughlin.
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions, and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions, and the disclaimer that follows 
    these conditions in the documentation and/or other materials 
    provided with the distribution.

 3. The name "JDOM" must not be used to endorse or promote products
    derived from this software without prior written permission.  For
    written permission, please contact <request_AT_jdom_DOT_org>.
 
 4. Products derived from this software may not be called "JDOM", nor
    may "JDOM" appear in their name, without prior written permission
    from the JDOM Project Management <request_AT_jdom_DOT_org>.
 
 In addition, we request (but do not require) that you include in the 
 end-user documentation provided with the redistribution and/or in the 
 software itself an acknowledgement equivalent to the following:
     "This product includes software developed by the
      JDOM Project (http://www.jdom.org/)."
 Alternatively, the acknowledgment may be graphical using the logos 
 available at http://www.jdom.org/images/logos.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 This software consists of voluntary contributions made by many 
 individuals on behalf of the JDOM Project and was originally 
 created by Jason Hunter <jhunter_AT_jdom_DOT_org> and
 Brett McLaughlin <brett_AT_jdom_DOT_org>.  For more information
 on the JDOM Project, please see <http://www.jdom.org/>.
 
 */

package org.jdom.transform;

import java.util.*;

import javax.xml.transform.sax.*;

import org.jdom.*;
import org.jdom.input.*;
import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;

/**
 * A holder for an XSL Transformation result, generally a list of nodes
 * although it can be a JDOM Document also. As stated by the XSLT 1.0
 * specification, the result tree generated by an XSL transformation is not
 * required to be a well-formed XML document. The result tree may have "any
 * sequence of nodes as children that would be possible for an
 * element node".
 * <p>
 * The following example shows how to apply an XSL Transformation
 * to a JDOM document and get the transformation result in the form
 * of a list of JDOM nodes:
 * <pre><code>
 *   public static List transform(Document doc, String stylesheet)
 *                                        throws JDOMException {
 *     try {
 *       Transformer transformer = TransformerFactory.newInstance()
 *                             .newTransformer(new StreamSource(stylesheet));
 *       JDOMSource in = new JDOMSource(doc);
 *       JDOMResult out = new JDOMResult();
 *       transformer.transform(in, out);
 *       return out.getResult();
 *     }
 *     catch (TransformerException e) {
 *       throw new JDOMException("XSLT Trandformation failed", e);
 *     }
 *   }
 * </code></pre>
 *
 * @see      org.jdom.transform.JDOMSource
 *
 * @version $Revision: 1.18 $, $Date: 2003/05/29 02:52:05 $
 * @author  Laurent Bihanic
 * @author  Jason Hunter
 */
public class JDOMResult extends SAXResult {

    private static final String CVS_ID =
    "@(#) $RCSfile: JDOMResult.java,v $ $Revision: 1.18 $ $Date: 2003/05/29 02:52:05 $ $Name:  $";

  /**
   * If {@link javax.xml.transform.TransformerFactory#getFeature}
   * returns <code>true</code> when passed this value as an
   * argument, the Transformer natively supports JDOM.
   * <p>
   * <strong>Note</strong>: This implementation does not override
   * the {@link SAXResult#FEATURE} value defined by its superclass
   * to be considered as a SAXResult by Transformer implementations
   * not natively supporting JDOM.</p>
   */
  public final static String JDOM_FEATURE =
                      "http://org.jdom.transform.JDOMResult/feature";

  /**
   * The result of a transformation, as set by Transformer
   * implementations that natively support JDOM, as a JDOM document
   * or a list of JDOM nodes.
   */
  private Object result = null;

  /**
   * Whether the application queried the result (as a list or a
   * document) since it was last set.
   */
  private boolean queried = false;

  /**
   * The custom JDOM factory to use when building the transformation
   * result or <code>null</code> to use the default JDOM classes.
   */
  private JDOMFactory factory = null;

  /**
   * Public default constructor.
   */
  public JDOMResult() {
    // Allocate custom builder object...
    DocumentBuilder builder = new DocumentBuilder();

    // And use it as ContentHandler and LexicalHandler.
    super.setHandler(builder);
    super.setLexicalHandler(builder);
  }

  /**
   * Sets the object(s) produced as result of an XSL Transformation.
   * <p>
   * <strong>Note</strong>: This method shall be used by the
   * {@link javax.xml.transform.Transformer} implementations that
   * natively support JDOM to directly set the transformation
   * result rather than considering this object as a
   * {@link SAXResult}.  Applications should <i>not</i> use this
   * method.</p>
   *
   * @param  result   the result of a transformation as a
   *                  {@link java.util.List list} of JDOM nodes
   *                  (Elements, Texts, Comments, PIs...).
   *
   * @see    #getResult
   */
  public void setResult(List result) {
    this.result  = result;
    this.queried = false;
  }

  /**
   * Returns the result of an XSL Transformation as a list of JDOM
   * nodes.
   * <p>
   * If the result of the transformation is a JDOM document,
   * this method converts it into a list of JDOM nodes; any
   * subsequent call to {@link #getDocument} will return
   * <code>null</code>.</p>
   *
   * @return the transformation result as a (possibly empty) list of
   *         JDOM nodes (Elements, Texts, Comments, PIs...).
   */
  public List getResult() {
    List nodes = Collections.EMPTY_LIST;

    // Retrieve result from the document builder if not set.
    this.retrieveResult();

    if (result instanceof List) {
      nodes = (List)result;
    }
    else {
      if ((result instanceof Document) && (queried == false)) {
        List content = ((Document)result).getContent();
        nodes = new ArrayList(content.size());

        while (content.size() != 0)
        {
          Object o = content.remove(0);
          nodes.add(o);
        }
        result = nodes;
      }
    }
    queried = true;

    return (nodes);
  }

  /**
   * Sets the document produced as result of an XSL Transformation.
   * <p>
   * <strong>Note</strong>: This method shall be used by the
   * {@link javax.xml.transform.Transformer} implementations that
   * natively support JDOM to directly set the transformation
   * result rather than considering this object as a
   * {@link SAXResult}.  Applications should <i>not</i> use this
   * method.</p>
   *
   * @param  document   the JDOM document result of a transformation.
   *
   * @see    #setResult
   * @see    #getDocument
   */
  public void setDocument(Document document) {
    this.result  = document;
    this.queried = false;
  }

  /**
   * Returns the result of an XSL Transformation as a JDOM document.
   * <p>
   * If the result of the transformation is a list of nodes,
   * this method attempts to convert it into a JDOM document. If
   * successful, any subsequent call to {@link #getResult} will
   * return an empty list.</p>
   * <p>
   * <strong>Warning</strong>: The XSLT 1.0 specification states that
   * the output of an XSL transformation is not a well-formed XML
   * document but a list of nodes. Applications should thus use
   * {@link #getResult} instead of this method or at least expect
   * <code>null</code> documents to be returned.
   *
   * @return the transformation result as a JDOM document or
   *         <code>null</code> if the result of the transformation
   *         can not be converted into a well-formed document.
   *
   * @see    #getResult
   */
  public Document getDocument() {
    Document doc = null;

    // Retrieve result from the document builder if not set.
    this.retrieveResult();

    if (result instanceof Document) {
      doc = (Document)result;
    }
    else {
      if ((result instanceof List) && (queried == false)) {
        // Try to create a document from the result nodes
        try {
          JDOMFactory f = this.getFactory();
          if (f == null) { f = new DefaultJDOMFactory(); }

          doc = f.document((Element)null);
          doc.setContent((List)result);

          result = doc;
        }
        catch (RuntimeException ex1) {
          // Some of the result nodes are not valid children of a
          // Document node. => return null.
        }
      }
    }
    queried = true;

    return (doc);
  }

  /**
   * Sets a custom JDOMFactory to use when building the
   * transformation result. Use a custom factory to build the tree
   * with your own subclasses of the JDOM classes.
   *
   * @param  factory   the custom <code>JDOMFactory</code> to use or
   *                   <code>null</code> to use the default JDOM
   *                   classes.
   *
   * @see    #getFactory
   */
  public void setFactory(JDOMFactory factory) {
    this.factory = factory;
  }

  /**
   * Returns the custom JDOMFactory used to build the transformation
   * result.
   *
   * @return the custom <code>JDOMFactory</code> used to build the
   *         transformation result or <code>null</code> if the
   *         default JDOM classes are being used.
   *
   * @see    #setFactory
   */
  public JDOMFactory getFactory() {
    return this.factory;
  }

  /**
   * Checks whether a transformation result has been set and, if not,
   * retrieves the result tree being built by the document builder.
   */
  private void retrieveResult() {
    if (result == null) {
      this.setResult(((DocumentBuilder)this.getHandler()).getResult());
    }
  }

  //-------------------------------------------------------------------------
  // SAXResult overwritten methods
  //-------------------------------------------------------------------------

  /**
   * Sets the target to be a SAX2 ContentHandler.
   *
   * @param handler Must be a non-null ContentHandler reference.
   */
  public void setHandler(ContentHandler handler) { }

  /**
   * Sets the SAX2 LexicalHandler for the output.
   * <p>
   * This is needed to handle XML comments and the like.  If the
   * lexical handler is not set, an attempt should be made by the
   * transformer to cast the ContentHandler to a LexicalHandler.</p>
   *
   * @param handler A non-null LexicalHandler for
   *                handling lexical parse events.
   */
  public void setLexicalHandler(LexicalHandler handler) { }


  //=========================================================================
  // FragmentHandler nested class
  //=========================================================================

  private static class FragmentHandler extends SAXHandler {
    /**
     * A dummy root element required by SAXHandler that can only
     * cope with well-formed documents.
     */
    private Element dummyRoot = new Element("root", null, null);

    /**
     * Public constructor.
     */
    public FragmentHandler(JDOMFactory factory) {
      super(factory);

      // Add a dummy root element to the being-built document as XSL
      // transformation can output node lists instead of well-formed
      // documents.
      this.getDocument().setRootElement(dummyRoot);
      setAlternateRoot(dummyRoot);
    }

    /**
     * Returns the result of an XSL Transformation.
     *
     * @return the transformation result as a (possibly empty) list of
     *         JDOM nodes (Elements, Texts, Comments, PIs...).
     */
    public List getResult() {
      return (this.getDetachedContent(dummyRoot));
    }

    /**
     * Returns the content of a JDOM Element detached from it.
     *
     * @param  elt   the element to get the content from.
     *
     * @return a (possibly empty) list of JDOM nodes, detached from
     *         their parent.
     */
    private List getDetachedContent(Element elt) {
      List content = elt.getContent();
      List nodes   = new ArrayList(content.size());

      while (content.size() != 0)
      {
        Object o = content.remove(0);
        nodes.add(o);
      }
      return (nodes);
    }
  }

  //=========================================================================
  // DocumentBuilder inner class
  //=========================================================================

  private class DocumentBuilder extends XMLFilterImpl
                                implements LexicalHandler {
    /**
     * The actual JDOM document builder.
     */
    private FragmentHandler saxHandler = null;

    /**
     * Whether the startDocument event was received. Some XSLT
     * processors such as Oracle's do not fire this event.
     */
    private boolean startDocumentReceived = false;

    /**
     * Public default constructor.
     */
    public DocumentBuilder() { }

    /**
     * Returns the result of an XSL Transformation.
     *
     * @return the transformation result as a (possibly empty) list of
     *         JDOM nodes (Elements, Texts, Comments, PIs...) or
     *         <code>null</code> if no new transformation occurred
     *         since the result of the previous one was returned.
     */
    public List getResult() {
      List result = null;

      if (this.saxHandler != null) {
        // Retrieve result from SAX content handler.
        result = this.saxHandler.getResult();

        // Detach the (non-reusable) SAXHandler instance.
        this.saxHandler = null;

        // And get ready for the next transformation.
        this.startDocumentReceived = false;
      }
      return result;
    }

    private void ensureInitialization() throws SAXException {
      // Trigger document initialization if XSLT processor failed to
      // fire the startDocument event.
      if (this.startDocumentReceived == false) {
        this.startDocument();
      }
    }

    //-----------------------------------------------------------------------
    // XMLFilterImpl overwritten methods
    //-----------------------------------------------------------------------

    /**
     * <i>[SAX ContentHandler interface support]</i> Processes a
     * start of document event.
     * <p>
     * This implementation creates a new JDOM document builder and
     * marks the current result as "under construction".</p>
     *
     * @throws SAXException   if any error occurred while creating
     *                        the document builder.
     */
    public void startDocument() throws SAXException {
      this.startDocumentReceived = true;

      // Reset any previously set result.
      setResult(null);

      // Create the actual JDOM document builder and register it as
      // ContentHandler on the superclass (XMLFilterImpl): this
      // implementation will take care of propagating the LexicalHandler
      // events.
      this.saxHandler = new FragmentHandler(getFactory());
      super.setContentHandler(this.saxHandler);

      // And propagate event.
      super.startDocument();
    }

    /**
     * <i>[SAX ContentHandler interface support]</i> Receives
     * notification of the beginning of an element.
     * <p>
     * This implementation ensures that startDocument() has been
     * called prior processing an element.
     *
     * @param  nsURI       the Namespace URI, or the empty string if
     *                     the element has no Namespace URI or if
     *                     Namespace processing is not being performed.
     * @param  localName   the local name (without prefix), or the
     *                     empty string if Namespace processing is
     *                     not being performed.
     * @param  qName       the qualified name (with prefix), or the
     *                     empty string if qualified names are not
     *                     available.
     * @param  atts        The attributes attached to the element.  If
     *                     there are no attributes, it shall be an
     *                     empty Attributes object.
     *
     * @throws SAXException   if any error occurred while creating
     *                        the document builder.
     */
    public void startElement(String nsURI, String localName, String qName,
                                           Attributes atts) throws SAXException
    {
      this.ensureInitialization();
      super.startElement(nsURI, localName, qName, atts);
    }

    /**
     * <i>[SAX ContentHandler interface support]</i> Begins the
     * scope of a prefix-URI Namespace mapping.
     */
    public void startPrefixMapping(String prefix, String uri)
                                                        throws SAXException {
      this.ensureInitialization();
      super.startPrefixMapping(prefix, uri);
    }

    /**
     * <i>[SAX ContentHandler interface support]</i> Receives
     * notification of character data.
     */
    public void characters(char ch[], int start, int length)
                                                        throws SAXException {
      this.ensureInitialization();
      super.characters(ch, start, length);
    }

    /**
     * <i>[SAX ContentHandler interface support]</i> Receives
     * notification of ignorable whitespace in element content.
     */
    public void ignorableWhitespace(char ch[], int start, int length)
                                                        throws SAXException {
      this.ensureInitialization();
      super.ignorableWhitespace(ch, start, length);
    }

    /**
     * <i>[SAX ContentHandler interface support]</i> Receives
     * notification of a processing instruction.
     */
    public void processingInstruction(String target, String data)
                                                        throws SAXException {
      this.ensureInitialization();
      super.processingInstruction(target, data);
    }

    /**
     * <i>[SAX ContentHandler interface support]</i> Receives
     * notification of a skipped entity.
     */
    public void skippedEntity(String name) throws SAXException {
      this.ensureInitialization();
      super.skippedEntity(name);
    }

    //-----------------------------------------------------------------------
    // LexicalHandler interface support
    //-----------------------------------------------------------------------

    /**
     * <i>[SAX LexicalHandler interface support]</i> Reports the
     * start of DTD declarations, if any.
     *
     * @param  name       the document type name.
     * @param  publicId   the declared public identifier for the
     *                    external DTD subset, or <code>null</code>
     *                    if none was declared.
     * @param  systemId   the declared system identifier for the
     *                    external DTD subset, or <code>null</code>
     *                    if none was declared.
     *
     * @throws SAXException   The application may raise an exception.
     */
    public void startDTD(String name, String publicId, String systemId)
                                        throws SAXException {
      this.ensureInitialization();
      this.saxHandler.startDTD(name, publicId, systemId);
    }

    /**
     * <i>[SAX LexicalHandler interface support]</i> Reports the end
     * of DTD declarations.
     *
     * @throws SAXException   The application may raise an exception.
     */
    public void endDTD() throws SAXException {
      this.saxHandler.endDTD();
    }

    /**
     * <i>[SAX LexicalHandler interface support]</i> Reports the
     * beginning of some internal and external XML entities.
     *
     * @param  name   the name of the entity.  If it is a parameter
     *                entity, the name will begin with '%', and if it
     *                is the external DTD subset, it will be "[dtd]".
     *
     * @throws SAXException   The application may raise an exception.
     */
    public void startEntity(String name) throws SAXException {
      this.ensureInitialization();
      this.saxHandler.startEntity(name);
    }

    /**
     * <i>[SAX LexicalHandler interface support]</i> Reports the end
     * of an entity.
     *
     * @param  name   the name of the entity that is ending.
     *
     * @throws SAXException   The application may raise an exception.
     */
    public void endEntity(String name) throws SAXException {
      this.saxHandler.endEntity(name);
    }

    /**
     * <i>[SAX LexicalHandler interface support]</i> Reports the
     * start of a CDATA section.
     *
     * @throws SAXException   The application may raise an exception.
     */
    public void startCDATA() throws SAXException {
      this.ensureInitialization();
      this.saxHandler.startCDATA();
    }

    /**
     * <i>[SAX LexicalHandler interface support]</i> Reports the end
     * of a CDATA section.
     *
     * @throws SAXException   The application may raise an exception.
     */
    public void endCDATA() throws SAXException {
      this.saxHandler.endCDATA();
    }

    /**
     * <i>[SAX LexicalHandler interface support]</i> Reports an XML
     * comment anywhere in the document.
     *
     * @param  ch     an array holding the characters in the comment.
     * @param  start  the starting position in the array.
     * @param  length the number of characters to use from the array.
     *
     * @throws SAXException   The application may raise an exception.
     */
    public void comment(char ch[], int start, int length)
                                  throws SAXException {
      this.ensureInitialization();
      this.saxHandler.comment(ch, start, length);
    }
  }
}

