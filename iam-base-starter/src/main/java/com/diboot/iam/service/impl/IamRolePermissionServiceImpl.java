package com.diboot.iam.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.diboot.core.util.BeanUtils;
import com.diboot.iam.entity.IamFrontendPermission;
import com.diboot.iam.entity.IamRolePermission;
import com.diboot.iam.mapper.IamRolePermissionMapper;
import com.diboot.iam.service.IamFrontendPermissionService;
import com.diboot.iam.service.IamRolePermissionService;
import com.diboot.iam.service.IamRoleService;
import com.diboot.iam.util.IamSecurityUtils;
import com.diboot.iam.vo.IamFrontendPermissionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* 角色权限关联相关Service实现
* @author mazc@dibo.ltd
* @version 2.0
* @date 2019-12-03
*/
@Service
@Slf4j
public class IamRolePermissionServiceImpl extends BaseIamServiceImpl<IamRolePermissionMapper, IamRolePermission> implements IamRolePermissionService {

    @Autowired
    private IamRoleService iamRoleService;

    @Autowired
    private IamFrontendPermissionService iamFrontendPermissionService;

    @Autowired
    private IamRolePermissionMapper iamRolePermissionMapper;

    @Override
    public List<IamFrontendPermissionVO> getPermissionVOList(String application, Long roleId) {
        List<Long> roleIdList = new ArrayList<>();
        roleIdList.add(roleId);
        return getPermissionVOList(application, roleIdList);
    }

    @Override
    public List<IamFrontendPermissionVO> getPermissionVOList(String application, List<Long> roleIds) {
        List<IamFrontendPermission> list = getPermissionList(application, roleIds);
        List<IamFrontendPermissionVO> voList = BeanUtils.convertList(list, IamFrontendPermissionVO.class);
        return BeanUtils.buildTree(voList);
    }

    @Override
    public List<IamFrontendPermission> getPermissionList(String application, List<Long> roleIds) {
        List<IamFrontendPermission> list = iamRolePermissionMapper.getPermissionsByRoleIds(roleIds);
        if(list == null){
            list = Collections.emptyList();
        }
        return list;
    }

    @Override
    public List<String> getApiUrlList(String application, List<Long> roleIds) {
        List<String> list = iamRolePermissionMapper.getApiUrlList(roleIds);
        if(list == null){
            list = Collections.emptyList();
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRolePermissionRelations(Long roleId, List<Long> permissionIdList) {
        // 批量创建
        List<IamRolePermission> rolePermissionList = new ArrayList<>();
        for(Long permissionId : permissionIdList){
            IamRolePermission rolePermission = new IamRolePermission(roleId, permissionId);
            rolePermissionList.add(rolePermission);
        }
        boolean success = createEntities(rolePermissionList);
        IamSecurityUtils.clearAllAuthorizationCache();
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRolePermissionRelations(Long roleId, List<Long> permissionIdList) {
        // 删除新列表中不存在的关联记录
        this.deleteEntities(
                Wrappers.<IamRolePermission>lambdaQuery()
                        .eq(IamRolePermission::getRoleId, roleId)
        );
        // 批量新增
        List<IamRolePermission> rolePermissionList = new ArrayList<>();
        for(Long permissionId : permissionIdList){
            IamRolePermission rolePermission = new IamRolePermission(roleId, permissionId);
            rolePermissionList.add(rolePermission);
        }
        boolean success = createEntities(rolePermissionList);
        IamSecurityUtils.clearAllAuthorizationCache();
        return success;
    }


    @Override
    public IamRoleService getRoleService() {
        return iamRoleService;
    }

    @Override
    public IamFrontendPermissionService getPermissionService() {
        return iamFrontendPermissionService;
    }
}
