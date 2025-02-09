package com.haishi.LittleRedBook.search.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.haishi.LittleRedBook.search.biz.domain.mapper.SelectMapper;
import com.haishi.LittleRedBook.search.biz.enums.NotePublishTimeRangeEnum;
import com.haishi.LittleRedBook.search.biz.enums.NoteSortTypeEnum;
import com.haishi.LittleRedBook.search.biz.index.NoteIndex;
import com.haishi.LittleRedBook.search.biz.model.vo.request.SearchNoteReqVO;
import com.haishi.LittleRedBook.search.biz.model.vo.response.SearchNoteRspVO;
import com.haishi.LittleRedBook.search.biz.service.NoteService;
import com.haishi.LittleRedBook.search.dto.RebuildNoteDocumentReqDTO;
import com.haishi.framework.commons.constant.DateConstants;
import com.haishi.framework.commons.response.PageResponse;
import com.haishi.framework.commons.response.Response;
import com.haishi.framework.commons.util.DateUtils;
import com.haishi.framework.commons.util.NumberUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @version: v1.0.0
 * @description: 用户搜索业务
 **/
@Service
@Slf4j
public class NoteServiceImpl implements NoteService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private SelectMapper selectMapper;

    /**
     * 搜索笔记
     *
     * @param searchNoteReqVO
     * @return
     */
    @Override
    public PageResponse<SearchNoteRspVO> searchNote(SearchNoteReqVO searchNoteReqVO) {
        // 查询关键词
        String keyword = searchNoteReqVO.getKeyword();
        // 当前页码
        Integer pageNo = searchNoteReqVO.getPageNo();
        // 笔记类型
        Integer type = searchNoteReqVO.getType();
        // 排序类型
        Integer sort = searchNoteReqVO.getSort();
        // 发布时间范围
        Integer publishTimeRange = searchNoteReqVO.getPublishTimeRange();


        // 构建 SearchRequest，指定要查询的索引
        SearchRequest searchRequest = new SearchRequest(NoteIndex.NAME);

        // 创建查询构建器
        SearchSourceBuilder sourceBuilder = searchSourceBuilderByCondition(keyword,type,sort,pageNo,publishTimeRange);
        // 将构建的查询条件设置到 SearchRequest 中
        searchRequest.source(sourceBuilder);

        // 返参 VO 集合
        List<SearchNoteRspVO> searchNoteRspVOS = null;
        // 总文档数，默认为 0
        long total = 0;
        try {
            log.info("==> SearchRequest: {}", searchRequest.source().toString());
            // 执行搜索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            // 处理搜索结果
            total = searchResponse.getHits().getTotalHits().value;
            log.info("==> 命中文档总数, hits: {}", total);

            searchNoteRspVOS = Lists.newArrayList();

            // 获取搜索命中的文档列表
            SearchHits hits = searchResponse.getHits();

            for (SearchHit hit : hits) {
                log.info("==> 文档数据: {}", hit.getSourceAsString());

                // 获取文档的所有字段（以 Map 的形式返回）
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();

                // 提取特定字段值
                Long noteId = (Long) sourceAsMap.get(NoteIndex.FIELD_NOTE_ID);
                String cover = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_COVER);
                String title = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_TITLE);
                String avatar = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_AVATAR);
                String nickname = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_NICKNAME);
                // 获取更新时间
                String updateTimeStr = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_UPDATE_TIME);
                LocalDateTime updateTime = LocalDateTime.parse(updateTimeStr, DateConstants.DATE_FORMAT_Y_M_D_H_M_S);
                Integer likeTotal = (Integer) sourceAsMap.get(NoteIndex.FIELD_NOTE_LIKE_TOTAL);
                Integer commentTotal = (Integer) sourceAsMap.get(NoteIndex.FIELD_NOTE_COMMENT_TOTAL);
                Integer collectTotal = (Integer) sourceAsMap.get(NoteIndex.FIELD_NOTE_COLLECT_TOTAL);


                // 获取高亮字段
                String highlightedTitle = null;
                if (CollUtil.isNotEmpty(hit.getHighlightFields())
                        && hit.getHighlightFields().containsKey(NoteIndex.FIELD_NOTE_TITLE)) {
                    highlightedTitle = hit.getHighlightFields().get(NoteIndex.FIELD_NOTE_TITLE).fragments()[0].string();
                }

                // 构建 VO 实体类
                SearchNoteRspVO searchNoteRspVO = SearchNoteRspVO.builder()
                        .noteId(noteId)
                        .cover(cover)
                        .title(title)
                        .highlightTitle(highlightedTitle)
                        .avatar(avatar)
                        .nickname(nickname)
                        .updateTime(DateUtils.formatRelativeTime(updateTime))
                        .likeTotal(NumberUtils.formatNumberString(likeTotal))
                        .commentTotal(NumberUtils.formatNumberString(commentTotal))
                        .collectTotal(NumberUtils.formatNumberString(collectTotal))
                        .build();

                searchNoteRspVOS.add(searchNoteRspVO);
            }
        } catch (IOException e) {
            log.error("==> 查询 Elasticserach 异常: ", e);
        }

        return PageResponse.success(searchNoteRspVOS, pageNo, total);
    }


    private SearchSourceBuilder searchSourceBuilderByCondition(String keyword, Integer type, Integer sort, Integer pageNo, Integer publishTimeRange) {
        SearchSourceBuilder sourceBuilder =new SearchSourceBuilder();

        // 创建查询条件
        //       "query": {
        //         "multi_match": {
        //           "query": "壁纸",
        //           "fields": ["title^2", "topic"]
        //         }
        //       },
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(
                QueryBuilders.multiMatchQuery(keyword)
                        .field(NoteIndex.FIELD_NOTE_TITLE, 2.0f) // 手动设置笔记标题的权重值为 2.0
                        .field(NoteIndex.FIELD_NOTE_TOPIC) // 不设置，权重默认为 1.0
        );

        // 按笔记类型过滤
        if (Objects.nonNull(type)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(NoteIndex.FIELD_NOTE_TYPE, type));
        }

        // 按发布时间范围过滤
        NotePublishTimeRangeEnum notePublishTimeRangeEnum = NotePublishTimeRangeEnum.valueOf(publishTimeRange);
        if (Objects.nonNull(notePublishTimeRangeEnum)) {
            // 结束时间
            String endTime = LocalDateTime.now().format(DateConstants.DATE_FORMAT_Y_M_D_H_M_S);
            // 开始时间
            String startTime = null;

            switch (notePublishTimeRangeEnum) {
                case DAY ->
                        startTime = DateUtils.localDateTime2String(LocalDateTime.now().minusDays(1)); // 一天之前的时间
                case WEEK ->
                        startTime = DateUtils.localDateTime2String(LocalDateTime.now().minusWeeks(1)); // 一周之前的时间
                case HALF_YEAR ->
                        startTime = DateUtils.localDateTime2String(LocalDateTime.now().minusMonths(6)); // 半年之前的时间
            }
            // 设置时间范围
            if (StringUtils.isNoneBlank(startTime)) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery(NoteIndex.FIELD_NOTE_CREATE_TIME)
                        .gte(startTime) // 大于等于
                        .lte(endTime) // 小于等于
                );
            }
        }

        // 排序
        NoteSortTypeEnum noteSortTypeEnum = NoteSortTypeEnum.valueOf(sort);


        // 综合排序，自定义评分，并按 _score 评分降序
        sourceBuilder.sort(new FieldSortBuilder("_score").order(SortOrder.DESC));
        // 设置排序
        // "sort": [
        //     {
        //       "_score": {
        //         "order": "desc"
        //       }
        //     }
        //   ]
        if (Objects.nonNull(noteSortTypeEnum)) {
            switch (noteSortTypeEnum) {
                // 按笔记发布时间降序
                case LATEST -> sourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_CREATE_TIME).order(SortOrder.DESC));
                // 按笔记点赞量降序
                case MOST_LIKE -> sourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_LIKE_TOTAL).order(SortOrder.DESC));
                // 按评论量降序
                case MOST_COMMENT -> sourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_COMMENT_TOTAL).order(SortOrder.DESC));
                // 按收藏量降序
                case MOST_COLLECT -> sourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_COLLECT_TOTAL).order(SortOrder.DESC));
            }
            // 设置查询
            sourceBuilder.query(boolQueryBuilder);
        } else { // 综合排序

            // 创建 FilterFunctionBuilder 数组
            // "functions": [
            //         {
            //           "field_value_factor": {
            //             "field": "like_total",
            //             "factor": 0.5,
            //             "modifier": "sqrt",
            //             "missing": 0
            //           }
            //         },
            //         {
            //           "field_value_factor": {
            //             "field": "collect_total",
            //             "factor": 0.3,
            //             "modifier": "sqrt",
            //             "missing": 0
            //           }
            //         },
            //         {
            //           "field_value_factor": {
            //             "field": "comment_total",
            //             "factor": 0.2,
            //             "modifier": "sqrt",
            //             "missing": 0
            //           }
            //         }
            //       ],
            FunctionScoreQueryBuilder.FilterFunctionBuilder[] filterFunctionBuilders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[] {
                    // function 1
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                            new FieldValueFactorFunctionBuilder(NoteIndex.FIELD_NOTE_LIKE_TOTAL)
                                    .factor(0.5f)
                                    .modifier(FieldValueFactorFunction.Modifier.SQRT)
                                    .missing(0)
                    ),
                    // function 2
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                            new FieldValueFactorFunctionBuilder(NoteIndex.FIELD_NOTE_COLLECT_TOTAL)
                                    .factor(0.3f)
                                    .modifier(FieldValueFactorFunction.Modifier.SQRT)
                                    .missing(0)
                    ),
                    // function 3
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                            new FieldValueFactorFunctionBuilder(NoteIndex.FIELD_NOTE_COMMENT_TOTAL)
                                    .factor(0.2f)
                                    .modifier(FieldValueFactorFunction.Modifier.SQRT)
                                    .missing(0)
                    )
            };

            // 构建 function_score 查询
            // "score_mode": "sum",
            // "boost_mode": "sum"
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(boolQueryBuilder,
                            filterFunctionBuilders)
                    .scoreMode(FunctionScoreQuery.ScoreMode.SUM) // score_mode 为 sum
                    .boostMode(CombineFunction.SUM); // boost_mode 为 sum

            // 设置查询
            sourceBuilder.query(functionScoreQueryBuilder);
        }


        // 设置分页，from 和 size
        int pageSize = 10; // 每页展示数据量
        int from = (pageNo - 1) * pageSize; // 偏移量
        sourceBuilder.from(from);
        sourceBuilder.size(pageSize);

        // 设置高亮字段
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(NoteIndex.FIELD_NOTE_TITLE)
                .preTags("<strong>") // 设置包裹标签
                .postTags("</strong>");
        sourceBuilder.highlighter(highlightBuilder);

        return sourceBuilder;
    }


    /**
     * 重建笔记文档
     *
     * @param rebuildNoteDocumentReqDTO
     * @return
     */
    @Override
    public Response<Long> rebuildDocument(RebuildNoteDocumentReqDTO rebuildNoteDocumentReqDTO) {
        Long noteId = rebuildNoteDocumentReqDTO.getId();

        // 从数据库查询 Elasticsearch 索引数据
        List<Map<String, Object>> result = selectMapper.selectEsNoteIndexData(noteId, null);

        // 遍历查询结果，将每条记录同步到 Elasticsearch
        for (Map<String, Object> recordMap : result) {
            // 创建索引请求对象，指定索引名称
            IndexRequest indexRequest = new IndexRequest(NoteIndex.NAME);
            // 设置文档的 ID，使用记录中的主键 “id” 字段值
            indexRequest.id((String.valueOf(recordMap.get(NoteIndex.FIELD_NOTE_ID))));
            // 设置文档的内容，使用查询结果的记录数据
            indexRequest.source(recordMap);
            // 将数据写入 Elasticsearch 索引
            try {
                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                log.error("==> 重建笔记文档失败: ", e);
            }
        }
        return Response.success();
    }
}