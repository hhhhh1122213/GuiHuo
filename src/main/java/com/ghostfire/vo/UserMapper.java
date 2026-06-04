package com.ghostfire.vo;

import com.ghostfire.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    SimpleUserVO toSimpleVO(User user);

    List<SimpleUserVO> toSimpleVOList(List<User> users);
}
