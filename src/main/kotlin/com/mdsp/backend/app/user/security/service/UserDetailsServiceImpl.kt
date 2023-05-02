package com.mdsp.backend.app.user.security.service

import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.structure.service.RolesGroupService
import com.mdsp.backend.app.user.model.UserPrincipal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.stream.Collectors

@Service
class UserDetailsServiceImpl: UserDetailsService {

    @Autowired
    lateinit var userRepository: IProfileRepository

    @Autowired
    lateinit var rolesGroupService: RolesGroupService

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsernameIgnoreCaseAndDeletedAtIsNull(username).get()
                ?: throw UsernameNotFoundException("User '$username' not found")

//        val authorities: List<GrantedAuthority> = user.getRoles()!!.stream().map({ role -> SimpleGrantedAuthority(role.name)}).collect(Collectors.toList<GrantedAuthority>())
        val roles = rolesGroupService.getRolesByProfileMap(user.getId()!!, arrayOf("key"))
        val authorities: List<GrantedAuthority> = roles.stream()
            .map { role -> SimpleGrantedAuthority("ROLE_${role["key"].toString().uppercase()}") }
            .collect(Collectors.toList<GrantedAuthority>())

        val userPrincipal = org.springframework.security.core.userdetails.User
            .withUsername(username)
            .password(user.pwd())
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(false)
            .build()

        return UserPrincipal(userPrincipal, user.getId()!!)
    }
}
