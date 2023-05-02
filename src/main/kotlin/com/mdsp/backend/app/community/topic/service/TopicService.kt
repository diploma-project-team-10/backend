package com.mdsp.backend.app.community.topic.service

import com.mdsp.backend.app.community.topic.model.Topic
import com.mdsp.backend.app.community.topic.repository.ITopicRepository
import java.sql.Timestamp
import java.util.*

class TopicService {

    companion object{
        fun getParentTopic(topic: Topic, topicRepository: ITopicRepository): Topic {
            var resultTopic = topic
            var parentTopic = topicRepository.findByIdAndDeletedAtIsNull(resultTopic.parentId!!)
            while(parentTopic.isPresent){
                resultTopic = parentTopic.get()
                parentTopic = topicRepository.findByIdAndDeletedAtIsNull(parentTopic.get().parentId!!)
            }
            return resultTopic
        }

        fun updateTopicVersionFromParent(parentTopic: Topic, topicVersion: Array<Int>, topicRepository: ITopicRepository){
            val newTopicVersion = topicVersion.plus(parentTopic.orderNum?:0)
            parentTopic.topicVersion = (newTopicVersion)
            topicRepository.save(parentTopic)
            val childrenTopics = topicRepository.findAllByParentIdAndDeletedAtIsNull(parentTopic.id)
            for(childTopic in childrenTopics){
                updateTopicVersionFromParent(childTopic, newTopicVersion, topicRepository)
            }
        }

        fun deleteAllChildrenTopics(topicId: UUID, topicRepository: ITopicRepository){
            val topicsQueue: Queue<UUID> = LinkedList()
            topicsQueue.add(topicId)
            while(topicsQueue.size != 0){
                val tempTopicId = topicsQueue.poll()
                val tempTopic = topicRepository.findByIdAndDeletedAtIsNull(tempTopicId).get()

                var childrenTopics = topicRepository.findAllByParentIdAndDeletedAtIsNull(tempTopicId)
                for(children in childrenTopics) topicsQueue.add(children.id)

                tempTopic.deletedAt = (Timestamp(System.currentTimeMillis()))
                topicRepository.save(tempTopic)
            }
        }

        fun getTopicTree(topic: Topic, topicRepository: ITopicRepository): TopicTree {
            if(topic.parentId == null){
                return TopicTree(topic.orderNum?:0, topic.id, arrayListOf(), topic.title!!)
            }
            var parentTopic: Topic = topicRepository.findByIdAndDeletedAtIsNull(topic.parentId!!).get()
            var parentTree = getTopicTree(parentTopic, topicRepository)
            parentTree.addChildrenTopic(topic)
            return parentTree
        }

    }
}
