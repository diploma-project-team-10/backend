package com.mdsp.backend.app.community.analytics.model

import com.mdsp.backend.app.community.analytics.payload.TopicAnalytics
import com.mdsp.backend.app.system.model.DateAudit
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

    var userId: UUID? = null

    @Type(type = "jsonb")
    @Column(
            name = "topics_analytics",
            columnDefinition = "jsonb"
    )
    var topicsAnalytics: ArrayList<TopicAnalytics> = arrayListOf()

    constructor(
            userId: UUID,
            topicsAnalytics: ArrayList<TopicAnalytics> = arrayListOf()
    ){
        this.userId = userId
        this.topicsAnalytics = topicsAnalytics
    }

    fun addCorrectAnswer(topicId: UUID){
        val temp = topicsAnalytics.find { topicAnalytics -> topicAnalytics.topicId!! == topicId }
        if(temp == null){
            val newTopicAnalytics = TopicAnalytics(topicId)
            newTopicAnalytics.addCorrectAnswer()
            topicsAnalytics.add(newTopicAnalytics)
        } else{
            temp.addCorrectAnswer()
        }
    }

    fun addWrongAnswer(topicId: UUID){
        val temp = topicsAnalytics.find { topicAnalytics -> topicAnalytics.topicId!! == topicId }
        if(temp == null){
            val newTopicAnalytics = TopicAnalytics(topicId)
            newTopicAnalytics.addWrongAnswer()
            topicsAnalytics.add(newTopicAnalytics)
        } else{
            temp.addWrongAnswer()
        }
    }

}
