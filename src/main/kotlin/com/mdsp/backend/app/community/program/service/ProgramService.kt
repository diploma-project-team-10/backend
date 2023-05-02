package com.mdsp.backend.app.community.program.service

import com.mdsp.backend.app.community.program.repository.IProgramRepository
import com.mdsp.backend.app.community.topic.model.Topic
import com.mdsp.backend.app.community.program.model.Program
import com.mdsp.backend.app.community.topic.repository.ITopicRepository
import com.mdsp.backend.app.community.topic.service.TopicService
import com.mdsp.backend.app.community.topic.service.TopicTree
import java.util.*

class ProgramService {
    companion object {

        fun deleteRelTopic(delTopic: Topic, programRepository: IProgramRepository) {
            val programs = programRepository.findAll()
            for(program in programs){
                val topics = program.relativeTopics
//                if(topics.contains(delTopic.id.toString())){
//                    val newTopics = arrayListOf<String>()
//                    for(topic in topics){
//                        if(topic != delTopic.id.toString())
//                            newTopics.add(topic)
//                    }
//                    program.relativeTopics = newTopics
//                    programRepository.save(program)
//                 }
            }
        }

        fun getProgramTopicTree(program: Program, topicRepository: ITopicRepository): TopicTree {
            val rootTree = TopicTree(-1, null, arrayListOf(), "ROOT")
//            val topicsArray: ArrayList<String> = program.relativeTopics

//            for(topic in topicsArray){
//                val topicId = UUID.fromString(topic)
//                val resultTopic = topicRepository.findByIdAndDeletedAtIsNull(topicId)
//                if(resultTopic.isPresent){
//                    val newResultTopic = resultTopic.get()
//                    val newTopicTree: TopicTree = TopicService.getTopicTree(newResultTopic, topicRepository)
//                    rootTree.compareAndAddChildrenTopicTree(newTopicTree)
//                }
//            }

            rootTree.sortChildren()
            return rootTree
        }
    }
}
