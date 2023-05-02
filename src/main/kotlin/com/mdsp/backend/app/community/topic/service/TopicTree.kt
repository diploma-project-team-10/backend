package com.mdsp.backend.app.community.topic.service

import com.mdsp.backend.app.community.topic.model.Topic
import com.mdsp.backend.app.community.topic.repository.ITopicRepository
import java.util.*
import kotlin.collections.ArrayList

class TopicTree: Comparable<TopicTree>{
    var orderNum: Int = 0
    var id: UUID? = null
    var childrenTopics: ArrayList<TopicTree> = arrayListOf()
    var title = ""

    constructor(
        orderNum: Int,
        id: UUID?,
        childrenTopics: ArrayList<TopicTree>,
        title: String
    ){
        this.orderNum = orderNum
        this.id = id
        this.childrenTopics = childrenTopics
        this.title = title
    }

    fun compareAndAddChildrenTopicTree(newTopicTree: TopicTree){
        for(childTopic in this.childrenTopics){
            if(childTopic.orderNum == newTopicTree.orderNum){
                for(newTopicChild in newTopicTree.childrenTopics){
                    childTopic.compareAndAddChildrenTopicTree(newTopicChild)
                }
                return
            }
        }
        this.childrenTopics.add(newTopicTree)
    }
    fun addChildrenTopic(topic: Topic){
        if(this.childrenTopics.size == 0){
            this.childrenTopics.add(TopicTree(topic.orderNum?:0, topic.id, arrayListOf(), topic.title!!))
            return
        }
        this.childrenTopics.get(0).addChildrenTopic(topic)
    }

    fun getTopicAtIndex(index: Int, topicRepository: ITopicRepository): Topic {
        var currentTopicTree = this.childrenTopics[index]
        while(currentTopicTree.childrenTopics.size != 0){
            currentTopicTree = currentTopicTree.childrenTopics[0]
        }
        return topicRepository.findByIdAndDeletedAtIsNull(currentTopicTree.id!!).get()
    }

    fun sortChildren(){
        this.childrenTopics.sort()
        for(child in this.childrenTopics) child.sortChildren()
    }

    fun toStringTree(){
        for(child in childrenTopics){
            child.toStringTree()
        }
    }

    override fun toString(): String {
        return "orderNum=${this.orderNum} id=${this.id} title=${this.title} children=${this.childrenTopics}"
    }

    override fun compareTo(other: TopicTree): Int {
        return this.orderNum - other.orderNum
    }
}
