package com.ava.frame.lucene.core;//package lucene.core;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class Searcher {

    private Integer page = 1;

    private Integer limit = 10;

    private Integer maxPage = 100;

    private DocumentDefinition documentDefinition;

    public Searcher(DocumentDefinition documentDefinition) {
        this.documentDefinition = documentDefinition;
    }

    public static Searcher build(Class clazz) {
        DocumentDefinition documentDefinition = DocumentContext.findDocumentDefinition(clazz);
        return new Searcher(documentDefinition);
    }


    private List<SortField> sortFields = new ArrayList<SortField>();

    private BooleanQuery.Builder query = new BooleanQuery.Builder();

    //    private Analyzer analyzer = new CJKAnalyzer();
    private Analyzer analyzer = new StandardAnalyzer();

    public Searcher page(Integer page) {
        this.page = page;
        return this;
    }

    public Searcher limit(Integer limit) {
        this.limit = limit;
        return this;
    }


    public Searcher all() {
        MatchAllDocsQuery all = new MatchAllDocsQuery();
        query.add(all, BooleanClause.Occur.SHOULD);
        return this;
    }

    //BooleanClause.Occur.MUST表示and,BooleanClause.Occur.MUST_NOT表示not,BooleanClause.Occur.SHOULD表示or.
    public Searcher must(String keyword, String field) {
        try {
            QueryParser parser = new QueryParser(field, analyzer);
            query.add(parser.parse(keyword), BooleanClause.Occur.MUST);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Searcher not(String keyword, String field) {
        try {
            QueryParser parser = new QueryParser(field, analyzer);
            query.add(parser.parse(keyword), BooleanClause.Occur.MUST_NOT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Searcher termMust(String field, String... value) {
        for (String s : value) {
            query.add(new TermQuery(new Term(field, s)), BooleanClause.Occur.MUST);
        }
        return this;
    }

    public Searcher termShould(String field, String... value) {
        for (String s : value) {
            query.add(new TermQuery(new Term(field, s)), BooleanClause.Occur.SHOULD);
        }
        return this;
    }


    public Searcher range(String filed, Integer min, Integer max) {
        query.add(IntPoint.newRangeQuery(filed, min, max), BooleanClause.Occur.MUST);
        return this;
    }

    /**
     * @param filed
     * @param min
     * @param max
     * @return
     * @author tfgao3
     */
    public Searcher range(String filed, Long min, Long max) {
        query.add(LongPoint.newRangeQuery(filed, min, max), BooleanClause.Occur.MUST);
        return this;
    }


    public Searcher fuzzy(String keyword, String... field) {
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        for (String s : field) {
            booleanQuery.add(new FuzzyQuery(new Term(s, keyword)), BooleanClause.Occur.SHOULD);
        }
        query.add(booleanQuery.build(), BooleanClause.Occur.SHOULD);
        return this;
    }

    public Searcher fuzzyMust(String keyword, String... field) {
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        for (String s : field) {
            booleanQuery.add(new FuzzyQuery(new Term(s, keyword)), BooleanClause.Occur.SHOULD);
        }
        query.add(booleanQuery.build(), BooleanClause.Occur.MUST);
        return this;
    }

    public Searcher prefix(String keyword, String... field) {
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        for (String s : field) {
            booleanQuery.add(new PrefixQuery(new Term(s, keyword)), BooleanClause.Occur.SHOULD);
        }
        query.add(booleanQuery.build(), BooleanClause.Occur.SHOULD);
        return this;
    }

    public Searcher prefixMust(String keyword, FieldInfo... field) {
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        for (FieldInfo s : field) {
            PrefixQuery prefixQuery = new PrefixQuery(new Term(s.getField(), keyword));
            booleanQuery.add(prefixQuery, BooleanClause.Occur.SHOULD);
        }
        query.add(booleanQuery.build(), BooleanClause.Occur.MUST);
        return this;
    }

    public Searcher must(FieldInfo... field) {
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        for (FieldInfo s : field) {
            TermQuery termQuery = new TermQuery(new Term(s.getField(), s.getKeyword()));
//            termQuery.setBoost(s.getBoost());
            booleanQuery.add(termQuery, BooleanClause.Occur.SHOULD);
        }
        query.add(booleanQuery.build(), BooleanClause.Occur.MUST);
        return this;
    }


    public Searcher prefixMust(String keyword, String... field) {
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        for (String s : field) {
            booleanQuery.add(new PrefixQuery(new Term(s, keyword)), BooleanClause.Occur.SHOULD);
        }
        query.add(booleanQuery.build(), BooleanClause.Occur.MUST);
        return this;
    }

    public Searcher wildcard(String keyword, String... field) {
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        for (String s : field) {
            booleanQuery.add(new WildcardQuery(new Term(s, "*" + keyword + "*")), BooleanClause.Occur.SHOULD);
        }
        query.add(booleanQuery.build(), BooleanClause.Occur.SHOULD);
        return this;
    }

    public Searcher wildcardMust(String keyword, String... field) {
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        for (String s : field) {
            booleanQuery.add(new WildcardQuery(new Term(s, "*" + keyword + "*")), BooleanClause.Occur.SHOULD);
        }
        query.add(booleanQuery.build(), BooleanClause.Occur.MUST);
        return this;
    }

    public Searcher wildcardMustStart(String keyword, String... field) {
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        for (String s : field) {
            booleanQuery.add(new WildcardQuery(new Term(s, keyword + "*")), BooleanClause.Occur.SHOULD);
        }
        query.add(booleanQuery.build(), BooleanClause.Occur.MUST);
        return this;
    }

    public Searcher should(String field, String keyword) {
        try {
            QueryParser parser = new QueryParser(field, analyzer);
            query.add(parser.parse(keyword), BooleanClause.Occur.SHOULD);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return this;
    }


    public Searcher asc(SortField.Type type, String... fields) {
        for (String field : fields) {
            sortFields.add(new SortField(field, type, false));
        }
        return this;
    }

    public Searcher asc(FieldComparatorSource fieldComparatorSource, String... fields) {
        for (String field : fields) {
            sortFields.add(new SortField(field, fieldComparatorSource, false));
        }
        return this;
    }


    public Searcher desc(FieldComparatorSource fieldComparatorSource, String... fields) {
        for (String field : fields) {
            sortFields.add(new SortField(field, fieldComparatorSource, true));
        }
        return this;
    }

    public Searcher desc(SortField.Type type, String... fields) {
        for (String field : fields) {
            sortFields.add(new SortField(field, type, true));
        }
        return this;
    }

    private static Logger log = org.slf4j.LoggerFactory.getLogger(Searcher.class);
    class SearcherHandlerImp implements SearcherHandler {
        @Override
        public QueryResult handler(IndexSearcher searcher) {
            try {
                BooleanQuery booleanQuery = query.build();
//                    log.info("query:" + booleanQuery.toString());
                TopDocs topDocs = null;
                if (sortFields.isEmpty()) {
                    topDocs = searcher.search(booleanQuery, maxPage * limit);// 只返回前maxPage * limit条记录
                } else {
                    Sort sort = new Sort(sortFields.toArray(new SortField[sortFields.size()]));
                    topDocs = searcher.search(booleanQuery, maxPage * limit, sort, true, true);// 只返回前maxPage * limit条记录
                }
                int total = topDocs.totalHits > maxPage * limit ? maxPage * limit : (int) topDocs.totalHits; // 搜索结果总数量
                ScoreDoc[] scoreDocs = topDocs.scoreDocs; // 搜索返回的结果集合
                int totalPage = ((total - 1) / limit) + 1;
                if (page > totalPage) {
                    return new QueryResult(0, 1, limit, new ArrayList());
                }
                int begin = (page - 1) * limit > total ? 0 : (page - 1) * limit;
                int end = page * limit > total ? total : page * limit;
                List list = new ArrayList();
                float maxscore=0;
                for (int i = begin; i < end; i++) {
                    ScoreDoc scoreDoc = scoreDocs[i];
                    if (i==begin){
                        maxscore= scoreDocs[begin].score;
                    }
                    Document doc = searcher.doc(scoreDoc.doc);
//                        log.info("doc:" + scoreDoc.doc + ",score:" + scoreDoc.score + ",boost:" + doc.get("boost"));
                    list.add(documentDefinition.documentToObj(doc));
                }

                return new QueryResult(total, page, limit, list,maxscore);
            } catch (Exception e) {
                log.error("searcher query", e);
            }
            return new QueryResult(0, 1, limit, new ArrayList());
        }
    }
    public QueryResult search() {
        return documentDefinition.search(new SearcherHandlerImp());
    }
//    public QueryResult searchByReader() {
//        return documentDefinition.searchByReader(new SearcherHandlerImp());
//    }
}
