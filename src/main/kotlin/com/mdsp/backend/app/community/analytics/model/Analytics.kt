package com.mdsp.backend.app.community.analytics.model

import com.mdsp.backend.app.community.analytics.payload.TopicAnalytics
import com.mdsp.backend.app.system.model.DateAudit
import com.mdsp.backend.app.system.model.Util
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList

@Entity
@Table(name = "community_analytics")
class Analytics: DateAudit {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    var id: UUID? = null

    @Column(name = "user_id")
    var userId: UUID? = null


    @Type(type = "jsonb")
    @Column(
            name = "topics_analytics",
            columnDefinition = "jsonb"
    )
    var topicsAnalytics: String? = null

    constructor(
            userId: UUID,
            topicsAnalytics: String?
    ){
        this.userId = userId
        this.topicsAnalytics = topicsAnalytics
    }

    fun getTopicsAnalytics(): ArrayList<TopicAnalytics> {
        val list: ArrayList<TopicAnalytics> = arrayListOf()
        val mapper = Util.toArrayMap(this.topicsAnalytics)
        for(map in mapper){
            val id: UUID = UUID.fromString(map["topicId"].toString())
            val correct: Int = map["correctAnswer"].toString().toInt()
            val wrong: Int = map["wrongAnswer"].toString().toInt()
            val percentage: Double = map["percentage"].toString().toDouble()
            val topicAnalytics: TopicAnalytics = TopicAnalytics(id, correct, wrong, percentage)
            list.add(topicAnalytics)
        }
        return list
    }
    fun setTopicsAnalytics(topicsAnalytics: ArrayList<TopicAnalytics>){
        var arrayMap: Array<MutableMap<String, Any?>> = emptyArray()
        for(topicAnalytic in topicsAnalytics){
            val temp = Util.objectToJson(topicAnalytic)
            arrayMap = arrayMap.plus(temp)
        }
        this.topicsAnalytics = Util.mapToString(arrayMap)
    }

    fun addCorrectAnswer(topicId: UUID){
        val list = getTopicsAnalytics()
        val temp = list.find { topicAnalytics -> topicAnalytics.topicId!! == topicId }
        if(temp == null){
            val newTopicAnalytics = TopicAnalytics(topicId)
            newTopicAnalytics.addCorrectAnswer()
            list.add(newTopicAnalytics)
        }else{
            temp.addCorrectAnswer()
        }
        setTopicsAnalytics(list)

    }

    fun addWrongAnswer(topicId: UUID){
        val list = getTopicsAnalytics()
        val temp = list.find { topicAnalytics -> topicAnalytics.topicId!! == topicId }
        if(temp == null){
            val newTopicAnalytics = TopicAnalytics(topicId)
            newTopicAnalytics.addWrongAnswer()
            list.add(newTopicAnalytics)
        }else{
            temp.addWrongAnswer()
        }
        setTopicsAnalytics(list)
    }

}
