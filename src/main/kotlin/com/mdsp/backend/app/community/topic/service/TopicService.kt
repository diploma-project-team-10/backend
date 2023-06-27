package com.mdsp.backend.app.community.topic.service

import com.mdsp.backend.app.community.topic.model.Topic
import com.mdsp.backend.app.community.topic.repository.ITopicRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.*

@Service
class TopicService {

    @Autowired
    lateinit var topicRepository: ITopicRepository

        final val ratingArray = arrayListOf(500, 50, 10, 1)

        fun getParentTopic(topic: Topic): Topic {
            var resultTopic = topic
            var parentTopic = topicRepository.findByIdAndDeletedAtIsNull(resultTopic.parentId!!)
            while(parentTopic.isPresent){
                resultTopic = parentTopic.get()
                parentTopic = topicRepository.findByIdAndDeletedAtIsNull(parentTopic.get().parentId!!)
            }
            return resultTopic
        }

        fun updateTopicVersionFromParent(parentTopic: Topic, topicVersion: Array<Int>){
            val newTopicVersion = topicVersion.plus(parentTopic.orderNum?:0)
            parentTopic.topicVersion = (newTopicVersion)

            parentTopic.rating = ratingArray.mapIndexed { index, value ->
                if (newTopicVersion.size > index) { (newTopicVersion[index]) * value } else 0
            }.sum()
            topicRepository.save(parentTopic)
            val childrenTopics = topicRepository.findAllByParentIdAndDeletedAtIsNull(parentTopic.id)
            for(childTopic in childrenTopics){
                updateTopicVersionFromParent(childTopic, newTopicVersion)
            }
        }

        fun deleteAllChildrenTopics(topicId: UUID){
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

        fun getTopicTree(topic: Topic): TopicTree {
            if(topic.parentId == null){
                return TopicTree(topic.orderNum?:0, topic.id, arrayListOf(), topic.title!!)
            }
            var parentTopic: Topic = topicRepository.findByIdAndDeletedAtIsNull(topic.parentId!!).get()
            var parentTree = getTopicTree(parentTopic)
            parentTree.addChildrenTopic(topic)
            return parentTree
        }

}
