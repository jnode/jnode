/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: XMLErrorResources_it.java,v 1.2.4.1 2005/09/15 07:45:42 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.res;


import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Set up error messages.
 * We build a two dimensional array of message keys and
 * message strings. In order to add a new message here,
 * you need to first add a String constant. And you need
 * to enter key, value pair as part of the contents
 * array. You also need to update MAX_CODE for error strings
 * and MAX_WARNING for warnings ( Needed for only information
 * purpose )
 */
public class XMLErrorResources_it extends ListResourceBundle
{

/*
 * This file contains error and warning messages related to Xalan Error
 * Handling.
 *
 *  General notes to translators:
 *
 *  1) Xalan (or more properly, Xalan-interpretive) and XSLTC are names of
 *     components.
 *     XSLT is an acronym for "XML Stylesheet Language: Transformations".
 *     XSLTC is an acronym for XSLT Compiler.
 *
 *  2) A stylesheet is a description of how to transform an input XML document
 *     into a resultant XML document (or HTML document or text).  The
 *     stylesheet itself is described in the form of an XML document.
 *
 *  3) A template is a component of a stylesheet that is used to match a
 *     particular portion of an input document and specifies the form of the
 *     corresponding portion of the output document.
 *
 *  4) An element is a mark-up tag in an XML document; an attribute is a
 *     modifier on the tag.  For example, in <elem attr='val' attr2='val2'>
 *     "elem" is an element name, "attr" and "attr2" are attribute names with
 *     the values "val" and "val2", respectively.
 *
 *  5) A namespace declaration is a special attribute that is used to associate
 *     a prefix with a URI (the namespace).  The meanings of element names and
 *     attribute names that use that prefix are defined with respect to that
 *     namespace.
 *
 *  6) "Translet" is an invented term that describes the class file that
 *     results from compiling an XML stylesheet into a Java class.
 *
 *  7) XPath is a specification that describes a notation for identifying
 *     nodes in a tree-structured representation of an XML document.  An
 *     instance of that notation is referred to as an XPath expression.
 *
 */

  /** Maximum error messages, this is needed to keep track of the number of messages.    */
  public static final int MAX_CODE = 61;

  /** Maximum warnings, this is needed to keep track of the number of warnings.          */
  public static final int MAX_WARNING = 0;

  /** Maximum misc strings.   */
  public static final int MAX_OTHERS = 4;

  /** Maximum total warnings and error messages.          */
  public static final int MAX_MESSAGES = MAX_CODE + MAX_WARNING + 1;


  /*
   * Message keys
   */
  public static final String ER_FUNCTION_NOT_SUPPORTED = "ER_FUNCTION_NOT_SUPPORTED";
  public static final String ER_CANNOT_OVERWRITE_CAUSE = "ER_CANNOT_OVERWRITE_CAUSE";
  public static final String ER_NO_DEFAULT_IMPL = "ER_NO_DEFAULT_IMPL";
  public static final String ER_CHUNKEDINTARRAY_NOT_SUPPORTED = "ER_CHUNKEDINTARRAY_NOT_SUPPORTED";
  public static final String ER_OFFSET_BIGGER_THAN_SLOT = "ER_OFFSET_BIGGER_THAN_SLOT";
  public static final String ER_COROUTINE_NOT_AVAIL = "ER_COROUTINE_NOT_AVAIL";
  public static final String ER_COROUTINE_CO_EXIT = "ER_COROUTINE_CO_EXIT";
  public static final String ER_COJOINROUTINESET_FAILED = "ER_COJOINROUTINESET_FAILED";
  public static final String ER_COROUTINE_PARAM = "ER_COROUTINE_PARAM";
  public static final String ER_PARSER_DOTERMINATE_ANSWERS = "ER_PARSER_DOTERMINATE_ANSWERS";
  public static final String ER_NO_PARSE_CALL_WHILE_PARSING = "ER_NO_PARSE_CALL_WHILE_PARSING";
  public static final String ER_TYPED_ITERATOR_AXIS_NOT_IMPLEMENTED = "ER_TYPED_ITERATOR_AXIS_NOT_IMPLEMENTED";
  public static final String ER_ITERATOR_AXIS_NOT_IMPLEMENTED = "ER_ITERATOR_AXIS_NOT_IMPLEMENTED";
  public static final String ER_ITERATOR_CLONE_NOT_SUPPORTED = "ER_ITERATOR_CLONE_NOT_SUPPORTED";
  public static final String ER_UNKNOWN_AXIS_TYPE = "ER_UNKNOWN_AXIS_TYPE";
  public static final String ER_AXIS_NOT_SUPPORTED = "ER_AXIS_NOT_SUPPORTED";
  public static final String ER_NO_DTMIDS_AVAIL = "ER_NO_DTMIDS_AVAIL";
  public static final String ER_NOT_SUPPORTED = "ER_NOT_SUPPORTED";
  public static final String ER_NODE_NON_NULL = "ER_NODE_NON_NULL";
  public static final String ER_COULD_NOT_RESOLVE_NODE = "ER_COULD_NOT_RESOLVE_NODE";
  public static final String ER_STARTPARSE_WHILE_PARSING = "ER_STARTPARSE_WHILE_PARSING";
  public static final String ER_STARTPARSE_NEEDS_SAXPARSER = "ER_STARTPARSE_NEEDS_SAXPARSER";
  public static final String ER_COULD_NOT_INIT_PARSER = "ER_COULD_NOT_INIT_PARSER";
  public static final String ER_EXCEPTION_CREATING_POOL = "ER_EXCEPTION_CREATING_POOL";
  public static final String ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE = "ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE";
  public static final String ER_SCHEME_REQUIRED = "ER_SCHEME_REQUIRED";
  public static final String ER_NO_SCHEME_IN_URI = "ER_NO_SCHEME_IN_URI";
  public static final String ER_NO_SCHEME_INURI = "ER_NO_SCHEME_INURI";
  public static final String ER_PATH_INVALID_CHAR = "ER_PATH_INVALID_CHAR";
  public static final String ER_SCHEME_FROM_NULL_STRING = "ER_SCHEME_FROM_NULL_STRING";
  public static final String ER_SCHEME_NOT_CONFORMANT = "ER_SCHEME_NOT_CONFORMANT";
  public static final String ER_HOST_ADDRESS_NOT_WELLFORMED = "ER_HOST_ADDRESS_NOT_WELLFORMED";
  public static final String ER_PORT_WHEN_HOST_NULL = "ER_PORT_WHEN_HOST_NULL";
  public static final String ER_INVALID_PORT = "ER_INVALID_PORT";
  public static final String ER_FRAG_FOR_GENERIC_URI ="ER_FRAG_FOR_GENERIC_URI";
  public static final String ER_FRAG_WHEN_PATH_NULL = "ER_FRAG_WHEN_PATH_NULL";
  public static final String ER_FRAG_INVALID_CHAR = "ER_FRAG_INVALID_CHAR";
  public static final String ER_PARSER_IN_USE = "ER_PARSER_IN_USE";
  public static final String ER_CANNOT_CHANGE_WHILE_PARSING = "ER_CANNOT_CHANGE_WHILE_PARSING";
  public static final String ER_SELF_CAUSATION_NOT_PERMITTED = "ER_SELF_CAUSATION_NOT_PERMITTED";
  public static final String ER_NO_USERINFO_IF_NO_HOST = "ER_NO_USERINFO_IF_NO_HOST";
  public static final String ER_NO_PORT_IF_NO_HOST = "ER_NO_PORT_IF_NO_HOST";
  public static final String ER_NO_QUERY_STRING_IN_PATH = "ER_NO_QUERY_STRING_IN_PATH";
  public static final String ER_NO_FRAGMENT_STRING_IN_PATH = "ER_NO_FRAGMENT_STRING_IN_PATH";
  public static final String ER_CANNOT_INIT_URI_EMPTY_PARMS = "ER_CANNOT_INIT_URI_EMPTY_PARMS";
  public static final String ER_METHOD_NOT_SUPPORTED ="ER_METHOD_NOT_SUPPORTED";
  public static final String ER_INCRSAXSRCFILTER_NOT_RESTARTABLE = "ER_INCRSAXSRCFILTER_NOT_RESTARTABLE";
  public static final String ER_XMLRDR_NOT_BEFORE_STARTPARSE = "ER_XMLRDR_NOT_BEFORE_STARTPARSE";
  public static final String ER_AXIS_TRAVERSER_NOT_SUPPORTED = "ER_AXIS_TRAVERSER_NOT_SUPPORTED";
  public static final String ER_ERRORHANDLER_CREATED_WITH_NULL_PRINTWRITER = "ER_ERRORHANDLER_CREATED_WITH_NULL_PRINTWRITER";
  public static final String ER_SYSTEMID_UNKNOWN = "ER_SYSTEMID_UNKNOWN";
  public static final String ER_LOCATION_UNKNOWN = "ER_LOCATION_UNKNOWN";
  public static final String ER_PREFIX_MUST_RESOLVE = "ER_PREFIX_MUST_RESOLVE";
  public static final String ER_CREATEDOCUMENT_NOT_SUPPORTED = "ER_CREATEDOCUMENT_NOT_SUPPORTED";
  public static final String ER_CHILD_HAS_NO_OWNER_DOCUMENT = "ER_CHILD_HAS_NO_OWNER_DOCUMENT";
  public static final String ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT = "ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT";
  public static final String ER_CANT_OUTPUT_TEXT_BEFORE_DOC = "ER_CANT_OUTPUT_TEXT_BEFORE_DOC";
  public static final String ER_CANT_HAVE_MORE_THAN_ONE_ROOT = "ER_CANT_HAVE_MORE_THAN_ONE_ROOT";
  public static final String ER_ARG_LOCALNAME_NULL = "ER_ARG_LOCALNAME_NULL";
  public static final String ER_ARG_LOCALNAME_INVALID = "ER_ARG_LOCALNAME_INVALID";
  public static final String ER_ARG_PREFIX_INVALID = "ER_ARG_PREFIX_INVALID";

  // Message keys used by the serializer
  public static final String ER_RESOURCE_COULD_NOT_FIND = "ER_RESOURCE_COULD_NOT_FIND";
  public static final String ER_RESOURCE_COULD_NOT_LOAD = "ER_RESOURCE_COULD_NOT_LOAD";
  public static final String ER_BUFFER_SIZE_LESSTHAN_ZERO = "ER_BUFFER_SIZE_LESSTHAN_ZERO";
  public static final String ER_INVALID_UTF16_SURROGATE = "ER_INVALID_UTF16_SURROGATE";
  public static final String ER_OIERROR = "ER_OIERROR";
  public static final String ER_NAMESPACE_PREFIX = "ER_NAMESPACE_PREFIX";
  public static final String ER_STRAY_ATTRIBUTE = "ER_STRAY_ATTIRBUTE";
  public static final String ER_STRAY_NAMESPACE = "ER_STRAY_NAMESPACE";
  public static final String ER_COULD_NOT_LOAD_RESOURCE = "ER_COULD_NOT_LOAD_RESOURCE";
  public static final String ER_COULD_NOT_LOAD_METHOD_PROPERTY = "ER_COULD_NOT_LOAD_METHOD_PROPERTY";
  public static final String ER_SERIALIZER_NOT_CONTENTHANDLER = "ER_SERIALIZER_NOT_CONTENTHANDLER";
  public static final String ER_ILLEGAL_ATTRIBUTE_POSITION = "ER_ILLEGAL_ATTRIBUTE_POSITION";

  /*
   * Now fill in the message text.
   * Then fill in the message text for that message code in the
   * array. Use the new error code as the index into the array.
   */

  // Error messages...

  /**
   * Get the lookup table for error messages
   *
   * @return The association list.
   */
  public Object[][] getContents()
  {
    return new Object[][] {

  /** Error message ID that has a null message, but takes in a single object.    */
    {"ER0000" , "{0}" },

    { ER_FUNCTION_NOT_SUPPORTED,
      "Funzione non supportata."},

    { ER_CANNOT_OVERWRITE_CAUSE,
      "Impossibile sovrascrivere causa"},

    { ER_NO_DEFAULT_IMPL,
      "Non \u00e8 stata trovata alcuna implementazione predefinita "},

    { ER_CHUNKEDINTARRAY_NOT_SUPPORTED,
      "ChunkedIntArray({0}) correntemente non supportato"},

    { ER_OFFSET_BIGGER_THAN_SLOT,
      "Offset pi\u00f9 grande dello slot"},

    { ER_COROUTINE_NOT_AVAIL,
      "Coroutine non disponibile, id={0}"},

    { ER_COROUTINE_CO_EXIT,
      "CoroutineManager ha ricevuto la richiesta co_exit()"},

    { ER_COJOINROUTINESET_FAILED,
      "co_joinCoroutineSet() con esito negativo"},

    { ER_COROUTINE_PARAM,
      "Errore parametro Coroutine {0})"},

    { ER_PARSER_DOTERMINATE_ANSWERS,
      "\nNON PREVISTO: Risposte doTerminate del parser {0}"},

    { ER_NO_PARSE_CALL_WHILE_PARSING,
      "impossibile richiamare l'analisi durante l''analisi"},

    { ER_TYPED_ITERATOR_AXIS_NOT_IMPLEMENTED,
      "Errore: iteratore immesso per l''asse {0} non implementato "},

    { ER_ITERATOR_AXIS_NOT_IMPLEMENTED,
      "Errore: iteratore per l''asse {0} non implementato "},

    { ER_ITERATOR_CLONE_NOT_SUPPORTED,
      "Clone iteratore non supportato"},

    { ER_UNKNOWN_AXIS_TYPE,
      "Tipo trasversale di asse sconosciuto: {0}"},

    { ER_AXIS_NOT_SUPPORTED,
      "Trasversale dell''asse non supportato: {0}"},

    { ER_NO_DTMIDS_AVAIL,
      "Non vi sono ulteriori ID DTM disponibili"},

    { ER_NOT_SUPPORTED,
      "Non supportato: {0}"},

    { ER_NODE_NON_NULL,
      "Il nodo deve essere non nullo per getDTMHandleFromNode"},

    { ER_COULD_NOT_RESOLVE_NODE,
      "Impossibile risolvere il nodo in un handle"},

    { ER_STARTPARSE_WHILE_PARSING,
       "Impossibile richiamare startParse durante l'analisi"},

    { ER_STARTPARSE_NEEDS_SAXPARSER,
       "startParse richiede SAXParser non nullo"},

    { ER_COULD_NOT_INIT_PARSER,
       "impossibile inizializzare il parser con"},

    { ER_EXCEPTION_CREATING_POOL,
       "si \u00e8 verificata un'eccezione durante la creazione della nuova istanza per il pool"},

    { ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE,
       "Il percorso contiene sequenza di escape non valida"},

    { ER_SCHEME_REQUIRED,
       "Lo schema \u00e8 obbligatorio."},

    { ER_NO_SCHEME_IN_URI,
       "Nessuno schema trovato nell''URI: {0}"},

    { ER_NO_SCHEME_INURI,
       "Non \u00e8 stato trovato alcuno schema nell'URI"},

    { ER_PATH_INVALID_CHAR,
       "Il percorso contiene un carattere non valido: {0}"},

    { ER_SCHEME_FROM_NULL_STRING,
       "Impossibile impostare lo schema da una stringa nulla"},

    { ER_SCHEME_NOT_CONFORMANT,
       "Lo schema non \u00e8 conforme."},

    { ER_HOST_ADDRESS_NOT_WELLFORMED,
       "Host non \u00e8 un'indirizzo corretto"},

    { ER_PORT_WHEN_HOST_NULL,
       "La porta non pu\u00f2 essere impostata se l'host \u00e8 nullo"},

    { ER_INVALID_PORT,
       "Numero di porta non valido"},

    { ER_FRAG_FOR_GENERIC_URI,
       "Il frammento pu\u00f2 essere impostato solo per un URI generico"},

    { ER_FRAG_WHEN_PATH_NULL,
       "Il frammento non pu\u00f2 essere impostato se il percorso \u00e8 nullo"},

    { ER_FRAG_INVALID_CHAR,
       "Il frammento contiene un carattere non valido"},

    { ER_PARSER_IN_USE,
      "Parser gi\u00e0 in utilizzo"},

    { ER_CANNOT_CHANGE_WHILE_PARSING,
      "Impossibile modificare {0} {1} durante l''analisi"},

    { ER_SELF_CAUSATION_NOT_PERMITTED,
      "Self-causation non consentito"},

    { ER_NO_USERINFO_IF_NO_HOST,
      "Userinfo non pu\u00f2 essere specificato se l'host non \u00e8 specificato"},

    { ER_NO_PORT_IF_NO_HOST,
      "La porta non pu\u00f2 essere specificata se l'host non \u00e8 specificato"},

    { ER_NO_QUERY_STRING_IN_PATH,
      "La stringa di interrogazione non pu\u00f2 essere specificata nella stringa di interrogazione e percorso."},

    { ER_NO_FRAGMENT_STRING_IN_PATH,
      "Il frammento non pu\u00f2 essere specificato sia nel percorso che nel frammento"},

    { ER_CANNOT_INIT_URI_EMPTY_PARMS,
      "Impossibile inizializzare l'URI con i parametri vuoti"},

    { ER_METHOD_NOT_SUPPORTED,
      "Metodo non ancora supportato "},

    { ER_INCRSAXSRCFILTER_NOT_RESTARTABLE,
      "IncrementalSAXSource_Filter correntemente non riavviabile"},

    { ER_XMLRDR_NOT_BEFORE_STARTPARSE,
      "XMLReader non si trova prima della richiesta startParse"},

    { ER_AXIS_TRAVERSER_NOT_SUPPORTED,
      "Trasversale dell''asse non supportato: {0}"},

    { ER_ERRORHANDLER_CREATED_WITH_NULL_PRINTWRITER,
      "ListingErrorHandler creato con PrintWriter nullo."},

    { ER_SYSTEMID_UNKNOWN,
      "SystemId sconosciuto"},

    { ER_LOCATION_UNKNOWN,
      "Posizione di errore sconosciuta"},

    { ER_PREFIX_MUST_RESOLVE,
      "Il prefisso deve risolvere in uno spazio nomi: {0}"},

    { ER_CREATEDOCUMENT_NOT_SUPPORTED,
      "createDocument() non supportato in XPathContext!"},

    { ER_CHILD_HAS_NO_OWNER_DOCUMENT,
      "Il secondario dell'attributo non ha un documento proprietario."},

    { ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT,
      "Il secondario dell'attributo non ha un elemento del documento proprietario."},

    { ER_CANT_OUTPUT_TEXT_BEFORE_DOC,
      "Attenzione: impossibile emettere testo prima dell'elemento del documento. Operazione ignorata..."},

    { ER_CANT_HAVE_MORE_THAN_ONE_ROOT,
      "Impossibile avere pi\u00f9 di una root in un DOM!"},

    { ER_ARG_LOCALNAME_NULL,
       "Argomento 'localName' nullo"},

    // Note to translators:  A QNAME has the syntactic form [NCName:]NCName
    // The localname is the portion after the optional colon; the message indicates
    // that there is a problem with that part of the QNAME.
    { ER_ARG_LOCALNAME_INVALID,
       "Localname in QNAME deve essere un NCName valido "},

    // Note to translators:  A QNAME has the syntactic form [NCName:]NCName
    // The prefix is the portion before the optional colon; the message indicates
    // that there is a problem with that part of the QNAME.
    { ER_ARG_PREFIX_INVALID,
       "Prefix in QNAME deve essere un NCName valido "},

    { "BAD_CODE", "Il parametro per createMessage fuori limite"},
    { "FORMAT_FAILED", "Rilevata eccezione durante la chiamata messageFormat"},
    { "line", "Riga #"},
    { "column","Colonna #"},

    {ER_SERIALIZER_NOT_CONTENTHANDLER,
      "La classe serializer ''{0}'' non implementa org.xml.sax.ContentHandler."},

    {ER_RESOURCE_COULD_NOT_FIND,
      "Risorsa [ {0} ] non trovata.\n {1}" },

    {ER_RESOURCE_COULD_NOT_LOAD,
      "Impossibile caricare la risorsa [ {0} ]: {1} \n {2} \t {3}" },

    {ER_BUFFER_SIZE_LESSTHAN_ZERO,
      "Dimensione buffer <=0" },

    {ER_INVALID_UTF16_SURROGATE,
      "Rilevato surrogato UTF-16 non valido: {0} ?" },

    {ER_OIERROR,
      "Errore IO" },

    {ER_ILLEGAL_ATTRIBUTE_POSITION,
      "Impossibile aggiungere l''attributo {0} dopo i nodi secondari o prima che sia prodotto un elemento. L''attributo verr\u00e0 ignorato. "},

      /*
       * Note to translators:  The stylesheet contained a reference to a
       * namespace prefix that was undefined.  The value of the substitution
       * text is the name of the prefix.
       */
    {ER_NAMESPACE_PREFIX,
      "Lo spazio nomi per il prefisso ''{0}'' non \u00e8 stato dichiarato. " },
      /*
       * Note to translators:  This message is reported if the stylesheet
       * being processed attempted to construct an XML document with an
       * attribute in a place other than on an element.  The substitution text
       * specifies the name of the attribute.
       */
    {ER_STRAY_ATTRIBUTE,
      "Attributo ''{0}'' al di fuori dell''elemento. " },

      /*
       * Note to translators:  As with the preceding message, a namespace
       * declaration has the form of an attribute and is only permitted to
       * appear on an element.  The substitution text {0} is the namespace
       * prefix and {1} is the URI that was being used in the erroneous
       * namespace declaration.
       */
    {ER_STRAY_NAMESPACE,
      "Dichiarazione dello spazio nome ''{0}''=''{1}'' al di fuori dell''elemento. " },

    {ER_COULD_NOT_LOAD_RESOURCE,
      "Impossibile caricare ''{0}'' (verificare CLASSPATH); verranno utilizzati i valori predefiniti "},

    {ER_COULD_NOT_LOAD_METHOD_PROPERTY,
      "Impossibile caricare il file delle propriet\u00e0 ''{0}'' per il metodo di emissione ''{1}'' (verificare CLASSPATH)" }


  };
  }

  /**
   *   Return a named ResourceBundle for a particular locale.  This method mimics the behavior
   *   of ResourceBundle.getBundle().
   *
   *   @param className the name of the class that implements the resource bundle.
   *   @return the ResourceBundle
   *   @throws MissingResourceException
   */
  public static final XMLErrorResources loadResourceBundle(String className)
          throws MissingResourceException
  {

    Locale locale = Locale.getDefault();
    String suffix = getResourceSuffix(locale);

    try
    {

      // first try with the given locale
      return (XMLErrorResources) ResourceBundle.getBundle(className
              + suffix, locale);
    }
    catch (MissingResourceException e)
    {
      try  // try to fall back to en_US if we can't load
      {

        // Since we can't find the localized property file,
        // fall back to en_US.
        return (XMLErrorResources) ResourceBundle.getBundle(className,
                new Locale("it", "IT"));
      }
      catch (MissingResourceException e2)
      {

        // Now we are really in trouble.
        // very bad, definitely very bad...not going to get very far
        throw new MissingResourceException(
          "Could not load any resource bundles.", className, "");
      }
    }
  }

  /**
   * Return the resource file suffic for the indicated locale
   * For most locales, this will be based the language code.  However
   * for Chinese, we do distinguish between Taiwan and PRC
   *
   * @param locale the locale
   * @return an String suffix which canbe appended to a resource name
   */
  private static final String getResourceSuffix(Locale locale)
  {

    String suffix = "_" + locale.getLanguage();
    String country = locale.getCountry();

    if (country.equals("TW"))
      suffix += "_" + country;

    return suffix;
  }

}
