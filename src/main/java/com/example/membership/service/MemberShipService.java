package com.example.membership.service;

import ch.qos.logback.core.joran.conditional.IfAction;
import com.example.membership.constant.Role;
import com.example.membership.dto.MemberShipDTO;
import com.example.membership.entity.Membership;
import com.example.membership.repository.MemberShipRepository;
import jakarta.persistence.Entity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class MemberShipService implements UserDetailsService {

    private final MemberShipRepository memberShipRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 : 컨트롤러에서 dto를 입력받아 entity로 변환하여 repository의
    // save를 이용해서 저장한다.
    // 반환값은 뭐로 하까? : dto전체를 반환으로 하자
    // 그른데~~~ 만약에 만약에 회원이 가입이 되어있으면 어쩔꺼임??

    public MemberShipDTO saveMember( MemberShipDTO memberShipDTO){

        // 사용자가 이미 있는지 확인
        // 가입하려는 email로 이미 사용자가 가입이 되어있는지 확인한다.
        Membership memberShip =
        memberShipRepository.findByEmail(memberShipDTO.getEmail());

        if (memberShip != null) {    //확인했더니 이미 가입이 되어있다면

            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
        memberShip =
            modelMapper.map(memberShipDTO, Membership.class);

        //일반유저
        memberShip.setRole(Role.ADMIN);
        // 비밀번호를 암호화 해서 저장한다.
        memberShip.setPassword(passwordEncoder.encode(memberShipDTO.getPassword()));

        memberShip =
            memberShipRepository.save(memberShip);      // 저장

        return modelMapper.map(memberShip, MemberShipDTO.class);

//        Entity entity =
//                modelMapper.map(dto, Entity.class)  // 예제는 이렇게
    }

    // 로그인  // UserDetailsService를 구현해서 사용한다.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 사용자가 입력한 email을 가지고 에서 검색한다.
        // 그러면 과 email를 password포함한 entity를 받을 것이다
        // 로그인은 기본적으로 email과 password를 찾거나
        // email로 검색해서 가져온 데이터가 null이 아니라면 그안에 있는 password를 가지고
        // 비교해서 맞다면 로그인한다.

        log.info("유저디테일 서비스로 들어온 이메일 : " + email); //안들어왔다면 input창으로 넣은 값이 도달을 못함
        Membership membership =
        this.memberShipRepository.findByEmail(email);
        // 이메일로 검색해서 가져온 값이 없다면 , 그러니까 회원가입이 되어 있지 않다면 예외 처리를 해주겠다.
        // try catch으로 다른 화면으로 덜리던 메세지를 가지고 로그인창으로 보내던 컨트롤러 창에서 알아서 해주겠지
        // 미래의 내가
        if(membership == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }

        // 예외처리가 되지 않았다면
        log.info("현재 되찾은 회원정보 " + membership);
        // 권한 처리
        String role = "";
        if ("ADMIN".equals(membership.getRole().name())) {
            log.info("관리자");
            role = Role.ADMIN.name();
        } else {
            log.info("일반유저");
            role = Role.USER.name();
        }

        return User.builder()
                .username(membership.getEmail())
                .password(membership.getPassword())     //<input name="password">
                .roles(role)
                .build();

    }
}