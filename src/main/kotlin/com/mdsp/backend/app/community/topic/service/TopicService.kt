package com.mdsp.backend.app.community.topic.service

import com.mdsp.backend.app.community.topic.model.Topic
import com.mdsp.backend.app.community.topic.repository.ITopicRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.*

@Service
class TopicService {

    @Autowired
    lateinit var topicRepository: ITopicRepository

    private final val RATING_ARRAY = arrayListOf(300, 150, 75, 30)
    private final val FIRST_TOPIC_RATING = 600

    fun getParentTopic(topic: Topic): Topic {
        if (topic.parentId == null) return topic
        var parentTopic = topicRepository.findByIdAndDeletedAtIsNull(topic.parentId!!)
        while (parentTopic.isPresent && parentTopic.get().parentId != null) {
            parentTopic = topicRepository.findByIdAndDeletedAtIsNull(parentTopic.get().parentId!!)
        }

        return parentTopic.get()
    }

    fun updateTopicVersionFromParent(parentTopic: Topic, topicVersion: Array<Int>) {
        val newTopicVersion = topicVersion.plus(parentTopic.orderNum ?: 0)
        parentTopic.topicVersion = newTopicVersion
        topicRepository.save(parentTopic)
        val childrenTopics = topicRepository.findAllByParentIdAndDeletedAtIsNull(parentTopic.id!!)
        for (childTopic in childrenTopics) {
            updateTopicVersionFromParent(childTopic, newTopicVersion)
        }
    }

    fun updateTopicVersionFromParent(parentTopic: Topic) {
        updateTopicVersionFromParent(parentTopic, emptyArray())
    }

    fun deleteAllChildrenTopics(topicId: UUID) {
        val topicsQueue: Queue<UUID> = LinkedList()
        topicsQueue.add(topicId)
        while (topicsQueue.size != 0) {
            val tempTopicId = topicsQueue.poll()
            val tempTopic = topicRepository.findByIdAndDeletedAtIsNull(tempTopicId).get()

            val childrenTopics = topicRepository.findAllByParentIdAndDeletedAtIsNull(tempTopicId)
            for (children in childrenTopics) topicsQueue.add(children.id)

            tempTopic.deletedAt = (Timestamp(System.currentTimeMillis()))
            topicRepository.save(tempTopic)
        }
    }

    fun getTopicTree(topic: Topic): TopicTree {
        if (topic.parentId == null) {
            return TopicTree(topic.orderNum ?: 0, topic.id, arrayListOf(), topic.title!!)
        }
        val parentTopic: Topic = topicRepository.findByIdAndDeletedAtIsNull(topic.parentId!!).get()
        val parentTree = getTopicTree(parentTopic)
        parentTree.addChildrenTopic(topic)
        return parentTree
    }

    @Async
    fun updateTopicVersionAndRating(topic: Topic) {
        val parentTopic = getParentTopic(topic)
        updateTopicVersionFromParent(parentTopic)
        updateTopicRatingsByProgramId(topic.programId!!)
    }

    fun updateTopicRatingsByProgramId(programId: UUID) {
        val currentRatingArray = arrayListOf(0, 0, 0, 0)
        topicRepository.findAllByProgramIdAndDeletedAtIsNullOrderByTopicVersion(programId).forEach { topic ->
            val indexVersion = topic.topicVersion.size - 1
            if (topic.topicVersion[indexVersion] != 0) {
                currentRatingArray[indexVersion]++
            }
            topic.rating = FIRST_TOPIC_RATING + currentRatingArray.mapIndexed { index, value ->
                RATING_ARRAY[index] * value
            }.sum()
            topicRepository.save(topic)
        }

    }

}
