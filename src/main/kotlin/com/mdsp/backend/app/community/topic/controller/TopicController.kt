package com.mdsp.backend.app.community.topic.controller

import com.mdsp.backend.app.community.program.repository.IProgramRepository
import com.mdsp.backend.app.community.program.service.ProgramService
import com.mdsp.backend.app.community.topic.model.Topic
import com.mdsp.backend.app.community.topic.repository.ITopicRepository
import com.mdsp.backend.app.community.topic.service.TopicService
import com.mdsp.backend.app.system.model.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/project/community/topic")
class TopicController {
    @Autowired
    lateinit var topicRepository: ITopicRepository

    @Autowired
    lateinit var programRepository: IProgramRepository


    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    fun getTopics() = topicRepository.findAllByDeletedAtIsNullOrderByOrderNum()

    @PostMapping("/new")
    @PreAuthorize("hasRole('COMMUNITY_ADMIN') or hasRole('ADMIN')")
    fun createTopic(
        @Valid @RequestBody newTopic: Topic,
    ): Status {
        val status = Status()
        status.status = 0
        status.message = ""

        val topicCandidate: Optional<Topic> = topicRepository.findByTitleIgnoreCaseAndParentIdAndDeletedAtIsNull(newTopic.title!!, newTopic.parentId)

        if(!topicCandidate.isPresent) {
            val _topic = Topic(
                    null,
                    newTopic.parentId,
                    newTopic.title!!.trim()
            )
            topicRepository.save(_topic)
            var siblingTopics = topicRepository.findAllByParentIdAndDeletedAtIsNull(_topic.parentId)
            _topic.orderNum = (siblingTopics.size)
            var parentTopic = TopicService.getParentTopic(_topic, topicRepository)
            TopicService.updateTopicVersionFromParent(parentTopic, emptyArray(), topicRepository)
            topicRepository.save(_topic)

            status.status = 1
            status.message = "New Topic created!"
            status.value = _topic.id
            return status
        } else {
            status.status = 0
            status.message = "Topic already exist!"
            return status
        }
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('COMMUNITY_ADMIN') or hasRole('ADMIN')")
    fun updateTopic(
        @Valid @RequestBody updTopic: Topic,
    ): Status {
        val status = Status()
        status.status = 0
        status.message = ""

        val topicCandidate: Optional<Topic> = topicRepository.findByIdAndDeletedAtIsNull(updTopic.id!!)

        if(topicCandidate.isPresent) {
            if(updTopic.title !== null) { topicCandidate.get().title = (updTopic.title) }
            topicRepository.save(topicCandidate.get())
            status.status = 1
            status.message = "Topic updated!"
            status.value = updTopic.id!!
            return status
        } else {
            status.status = 0
            status.message = "Topic does not exist!"
            return status
        }
    }

    @PostMapping("/update/order")
    @PreAuthorize("hasRole('COMMUNITY_ADMIN') or hasRole('ADMIN')")
    fun updateTopicOrder(@Valid @RequestBody updTopic: ArrayList<Topic>): Status {
        val status = Status()
        status.status = 0
        status.message = ""

        if (updTopic.isNotEmpty()) {
            var isParentTopic: Boolean = updTopic.get(0).parentId == null
            for(topic in updTopic) {
                val topicCandidate: Optional<Topic> = topicRepository.findByIdAndDeletedAtIsNull(topic.id!!)

                if (topicCandidate.isPresent) {
                    topicCandidate.get().orderNum = (topic.orderNum)
                    if(isParentTopic) TopicService.updateTopicVersionFromParent(topicCandidate.get(), emptyArray<Int>(), topicRepository)
                    topicRepository.save(topicCandidate.get())
                }
            }

            if(!isParentTopic){
                var parentTopic = TopicService.getParentTopic(updTopic.get(0), topicRepository)
                TopicService.updateTopicVersionFromParent(parentTopic, emptyArray<Int>(), topicRepository)
            }

            status.status = 1
            status.message = "Topic's order updated!"
            return status
        } else {
            status.status = 0
            status.message = "Topic list is empty!"
            return status
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('COMMUNITY_ADMIN') or hasRole('ADMIN')")
    fun deleteTopic(@PathVariable(value = "id") id: UUID): Status {
        val status = Status()
        status.status = 0
        status.message = ""

        val topicCandidate: Optional<Topic> = topicRepository.findByIdAndDeletedAtIsNull(id)

        if(topicCandidate.isPresent) {
            val delTopic = topicCandidate.get()
            val isParentTopic: Boolean = delTopic.parentId == null

            val siblingTopics = topicRepository.findAllByParentIdAndDeletedAtIsNull(delTopic.parentId)
            for(topic in siblingTopics){
                if((topic.orderNum ?: 0) > (delTopic.orderNum?:0)){
                    topic.orderNum = ((topic.orderNum?:0)-1)
                    if(isParentTopic) TopicService.updateTopicVersionFromParent(topic, emptyArray(), topicRepository)
                }
            }

            if(!isParentTopic){
                val parentTopic = TopicService.getParentTopic(delTopic, topicRepository)
                TopicService.updateTopicVersionFromParent(parentTopic, emptyArray(), topicRepository)
            }

            TopicService.deleteAllChildrenTopics(delTopic.id!!, topicRepository)
            ProgramService.deleteRelTopic(delTopic, programRepository)
        } else {
            status.status = 0
            status.message = "Topic does not exist!"
            return status
        }
        status.status = 1
        status.message = "Topic deleted!"
        return status
    }

}


