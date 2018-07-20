  package com.ava.frame.lucene.core;



import com.ava.frame.lucene.IndexCase;
import com.ava.frame.lucene.LuceneException;
import com.ava.frame.lucene.annotation.IndexDomain;
import com.ava.frame.lucene.annotation.IndexFiled;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


  public class DocumentDefinition {

      private static Log log = LogFactory.getLog(DocumentDefinition.class);
      private DirectoryReader indexReader;
      private IndexSearcher searcherReader;
      private SearcherManager mgr;

      private IndexWriter tkWriter;

      public void close() {
          try {
              mgr.close();
              tkWriter.close();
          } catch (IOException e) {
              e.printStackTrace();
          }
          tkWriter = null;
          mgr = null;
      }

      public void release(IndexSearcher searcher) {
          try {
              mgr.release(searcher);
          } catch (IOException e) {
              log.error("release", e);
          }
      }


      public void index(DataRunner dataRunner) {
          dataRunner.handler(this);
          commit();
      }

      public QueryResult search(SearcherHandler searcherHandler) {
          try {
              if (searcherReader != null) {
                  return searcherHandler.handler(searcherReader);
              }

          } catch (Exception e) {
              e.printStackTrace();
          }
          IndexSearcher searcher = null;
          try {
              //更新看看内存中索引是否有变化如果，有一个更新了，其他线程也会更新
              mgr.maybeRefresh();
              //利用acquire 方法获取search，执行此方法前须执行maybeRefresh
              searcher = mgr.acquire();
              return searcherHandler.handler(searcher);
          } catch (Exception e) {
              log.error("search", e);
          } finally {
              try {
                  //释放searcher，
                  mgr.release(searcher);
              } catch (Exception e) {
                  log.error("search", e);
              }
          }
          return null;
      }

      public QueryResult searchByReader(SearcherHandler searcherHandler) {
          IndexSearcher searcher = null;
          try {
              if (indexReader != null && !indexReader.isCurrent()) {
                  indexReader = DirectoryReader.openIfChanged(indexReader);
              }
              searcher = new IndexSearcher(indexReader);
              return searcherHandler.handler(searcher);
          } catch (Exception e) {
              log.error("search", e);
          }
          return null;
      }

      public void deleteAll() {
          try {
              tkWriter.deleteAll();
          } catch (IOException e) {
              log.error("deleteAll:", e);
          }
      }


      public void deleteDocument(Term term) {
          try {
              tkWriter.deleteDocuments(term);
          } catch (IOException e) {
              log.error("write:", e);
          }
      }

      public void deleteDocument(Query query) {
          try {
              tkWriter.deleteDocuments(query);
          } catch (IOException e) {
              log.error("write:", e);
          }
      }

      public void commit() {
          try {
              tkWriter.commit();
          } catch (Exception e) {
              e.printStackTrace();
          }
      }

      public void addDocument(Object obj) {
          try {
              tkWriter.addDocument(objToDocument(obj));
          } catch (IOException e) {
              log.error("write:", e);
          }
      }

      public void current() {
          try {
              if (indexReader != null && !indexReader.isCurrent()) {
                  indexReader = DirectoryReader.openIfChanged(indexReader);
                  searcherReader = new IndexSearcher(indexReader);
              }
          } catch (IOException e) {
              log.error(e.getMessage());
          }
      }

  /*
      public void readFile() throws IOException {
          MultiReader multiReader;
          String path = MessageSource.message(base_path) + "/index/" + clazz.getSimpleName().toLowerCase();
          File file = new File(path);
          File[] files = file.listFiles();
          IndexReader[] readers = new IndexReader[files.length];
          if(!realtime){
              for (int i = 0 ; i < files.length ; i ++) {
                  readers[i] = DirectoryReader.open(FSDirectory.open(files[i]));
              }
          }else{
              for (int i = 0 ; i < files.length ; i ++) {
                  readers[i] = DirectoryReader.open(IndexUtil.getIndexWriter(files[i].getPath(), true), true);
              }
          }

          multiReader = new MultiReader(readers);
          IndexSearcher searcherReader = new IndexSearcher(multiReader,service);
      }
  */


      public static class FiledDefinition {
          private String filedName;
          private Index.Field filedType;
          private Class type;
          private IndexCase indexCase;
          private Index.Store store;

          public FiledDefinition(String filedName, Index.Field filedType, Class type, IndexCase indexCase, Index.Store store) {
              this.filedName = filedName;
              this.filedType = filedType;
              this.type = type;
              this.indexCase = indexCase;
              this.store = store;
          }


          public String getFiledName() {
              return filedName;
          }

          public void setFiledName(String filedName) {
              this.filedName = filedName;
          }

          public Index.Field getFiledType() {
              return filedType;
          }

          public void setFiledType(Index.Field filedType) {
              this.filedType = filedType;
          }

          public Object stringValue(Class ref, Document document) {
              String value = document.get(filedName);
              if (ref.isEnum()) {
                  Object[] ts = ref.getEnumConstants();
                  for (Object obj : ts) {
                      if (value.equals(obj.toString())) {
                          return obj;
                      }
                  }
                  return null;
              } else {
                  return value;
              }
          }

          public Object realValue(Document document) {
              if (filedType.equals(Index.Field.String) || filedType.equals(Index.Field.Text)) {
                  return stringValue(type, document);
              } else if (filedType.equals(Index.Field.Int)) {
                  String value = document.get(filedName);
                  if (value == null) {
                      return null;
                  }
                  return Integer.valueOf(value);
              } else if (filedType.equals(Index.Field.Long)) {
                  String value = document.get(filedName);
                  if (value == null) {
                      return null;
                  }
                  return Long.valueOf(value);
              } else if (filedType.equals(Index.Field.Double)) {
                  String value = document.get(filedName);
                  if (value == null) {
                      return null;
                  }
                  return Double.valueOf(value);
              } else if (filedType.equals(Index.Field.Float)) {
                  String value = document.get(filedName);
                  if (value == null) {
                      return null;
                  }
                  return Float.valueOf(value);
              } else if (filedType.equals(Index.Field.Array)) {
                  String[] value = document.getValues(filedName);
                  return new HashSet<String>(Arrays.asList(value));
              }else if (filedType.equals(Index.Field.ArrayText)) {
                  String[] value = document.getValues(filedName);
                  return new HashSet<String>(Arrays.asList(value));
              } else if (filedType.equals(Index.Field.List)) {
                  String[] value = document.getValues(filedName);
                  return new ArrayList<String>(Arrays.asList(value));
              } else if (filedType.equals(Index.Field.Boolean)) {
                  String value = document.get(filedName);
                  if (value == null) {
                      return false;
                  }
                  return Boolean.valueOf(value);
              } else {
                  throw new LuceneException("not type found");
              }

          }

  //        public String caseString(String str){
  //               if(IndexCase.lower.equals(indexCase)){
  //                   return str.toLowerCase();
  //               }else if(IndexCase.upper.equals(indexCase)){
  //                   return str.toUpperCase();
  //               }else if(){{
  //                   return str;
  //               }
  //        }

          public void caseString(Document document, String value) {
              StringField stringField = new StringField(filedName, value, store.store());
              document.add(stringField);
  //            SearchField searchField = new SearchField(filedName, value);
  //            document.add(searchField);
          }

          public void assembleStringValue(Document document, String value) {
              if (IndexCase.lower.equals(indexCase)) {
                  caseString(document, value.toLowerCase());
              } else if (IndexCase.upper.equals(indexCase)) {
                  caseString(document, value.toUpperCase());
              } else if (IndexCase.both.equals(indexCase)) {
                  caseString(document, value);
                  caseString(document, value.toUpperCase());
                  caseString(document, value.toLowerCase());
              } else {
                  caseString(document, value);
              }
          }

          public void assembleValue(Document document, String... value) {
              if (filedType.equals(Index.Field.String)) {
                  assembleStringValue(document, value[0]);
              }
              if (filedType.equals(Index.Field.Boolean)) {
                  document.add(new StoredField(filedName, value[0]));
              } else if (filedType.equals(Index.Field.Int)) {
                  document.add(new IntPoint(filedName, Integer.valueOf(value[0])));
                  document.add(new StoredField(filedName, Integer.valueOf(value[0])));
              } else if (filedType.equals(Index.Field.Long)) {
                  document.add(new LongPoint(filedName, Long.valueOf(value[0])));
                  document.add(new StoredField(filedName, Integer.valueOf(value[0])));
              } else if (filedType.equals(Index.Field.Double)) {
                  document.add(new DoublePoint(filedName, Double.valueOf(value[0])));
                  document.add(new StoredField(filedName, Integer.valueOf(value[0])));
              } else if (filedType.equals(Index.Field.Float)) {
                  document.add(new FloatPoint(filedName, Float.valueOf(value[0])));
                  document.add(new StoredField(filedName, Integer.valueOf(value[0])));
              } else if (filedType.equals(Index.Field.Array) || filedType.equals(Index.Field.List)) {
                  for (String str : value) {
                      assembleStringValue(document, str);
                  }
              } else if (filedType.equals(Index.Field.Text)) {
                  document.add(new TextField(filedName, String.valueOf(value[0]), org.apache.lucene.document.Field.Store.YES));
              } else if (filedType.equals(Index.Field.ArrayText)) {
                  for (String str : value) {
                      document.add(new TextField(filedName,str, org.apache.lucene.document.Field.Store.YES));
                  }
              }
          }
      }


      private Class clazz;
      private String domain;
      private List<FiledDefinition> filedDefinitions;

      public void addFiledDefinition(FiledDefinition filedDefinition) {
          if (filedDefinitions == null) {
              filedDefinitions = new ArrayList<FiledDefinition>();
          }
          filedDefinitions.add(filedDefinition);
      }

      public String getDomain() {
          return domain;
      }

      public void setDomain(String domain) {
          this.domain = domain;
      }

      public Class getClazz() {
          return clazz;
      }

      public void setClazz(Class clazz) {
          this.clazz = clazz;
      }

      public List<FiledDefinition> getFiledDefinitions() {
          return filedDefinitions;
      }

      public void setFiledDefinitions(List<FiledDefinition> filedDefinitions) {
          this.filedDefinitions = filedDefinitions;
      }


      public DocumentDefinition(String indexPath, Class clazz, boolean byReaderSearcher) {
          if (byReaderSearcher) {
              initReaderSearcher(indexPath, clazz);
          } else {
              init(indexPath, clazz);
          }
      }


      private void initReaderSearcher(String indexPath, Class clazz) {
          try {
              String path = indexPath + "index/" + clazz.getSimpleName().toLowerCase();
              Directory indexDir = NIOFSDirectory.open(FileSystems.getDefault().getPath(path));
              indexReader = DirectoryReader.open(indexDir);
              searcherReader = new IndexSearcher(indexReader);
          } catch (Exception ex) {
              log.error("init", ex);
          }
          if (clazz.isAnnotationPresent(IndexDomain.class)) {
              IndexDomain indexDomain = (IndexDomain) clazz.getAnnotation(IndexDomain.class);
              setClazz(clazz);
              setDomain(indexDomain.domain());
              field2DocumentDefinition(clazz.getDeclaredFields(), this);
              if (clazz.getSuperclass() != null) {
                  field2DocumentDefinition(clazz.getSuperclass().getDeclaredFields(), this);
              }
          } else {
              throw new LuceneException("this class " + clazz.getSimpleName() + " not @IndexDomain");
          }
      }

      private void init(String indexPath, Class clazz) {
          try {
              String path = indexPath + "index/" + clazz.getSimpleName().toLowerCase();
              Directory indexDir = NIOFSDirectory.open(FileSystems.getDefault().getPath(path));
              Analyzer luceneAnalyzer = new StandardAnalyzer();
              IndexWriterConfig indexWriterConfig = new IndexWriterConfig(luceneAnalyzer);
  //            indexWriterConfig.setCommitOnClose(true);
  //            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
              tkWriter = new IndexWriter(indexDir, indexWriterConfig);

              //新建SearcherManager
              //true 表示在内存中删除，false可能删可能不删，设为false性能会更好一些
              mgr = new SearcherManager(tkWriter, false, false, new SearcherFactory());

          } catch (Exception ex) {
              log.error("init", ex);
          }

          if (clazz.isAnnotationPresent(IndexDomain.class)) {
              IndexDomain indexDomain = (IndexDomain) clazz.getAnnotation(IndexDomain.class);
              setClazz(clazz);
              setDomain(indexDomain.domain());
              field2DocumentDefinition(clazz.getDeclaredFields(), this);
              if (clazz.getSuperclass() != null) {
                  field2DocumentDefinition(clazz.getSuperclass().getDeclaredFields(), this);
              }
          } else {
              throw new LuceneException("this class " + clazz.getSimpleName() + " not @IndexDomain");
          }
      }


      private void field2DocumentDefinition(Field[] fields, DocumentDefinition documentDefinition) {
          for (Field field : fields) {
              if (field.isAnnotationPresent(IndexFiled.class)) {
                  IndexFiled indexFiled = field.getAnnotation(IndexFiled.class);
                  documentDefinition.addFiledDefinition(new FiledDefinition(field.getName(), indexFiled.field(), field.getType(), indexFiled.indexCase(), indexFiled.store()));
              }
          }
      }


      protected <T> T documentToObj(Document document) {
          try {
              Object obj = getClazz().newInstance();
              for (FiledDefinition filedDefinition : filedDefinitions) {
                  PropertyUtils.setProperty(obj, filedDefinition.filedName, filedDefinition.realValue(document));
              }
              return (T) obj;
          } catch (Exception e) {
              throw new LuceneException(e);
          }
      }

      protected Document objToDocument(Object obj) {
          try {
              Document document = new Document();
              for (FiledDefinition filedDefinition : filedDefinitions) {
                  if (filedDefinition.getFiledType().equals(Index.Field.Array) || filedDefinition.getFiledType().equals(Index.Field.List)|| filedDefinition.getFiledType().equals(Index.Field.ArrayText)) {
                      String[] value = BeanUtils.getArrayProperty(obj, filedDefinition.filedName);
                      if (value != null) {
                          filedDefinition.assembleValue(document, value);
                      }
                  } else {
                      String value = BeanUtils.getProperty(obj, filedDefinition.filedName);
                      if (value != null) {
                          filedDefinition.assembleValue(document, value);
                      }
                  }
              }

              return document;
          } catch (Exception ex) {
              throw new LuceneException(ex);
          }

      }
  }
