package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.Medal;
import java.util.List;

public interface MedalService extends IService<Medal> {

    /** 全部勋章定义 */
    List<Medal> listAll();

    /** 用户已获得的勋章 */
    List<Medal> listUserMedals(Long userId);

    /** 管理员手动颁发 */
    void awardManual(Long userId, Long medalId);

    /** 检查并自动颁发 AUTO_STAT 类勋章 */
    void checkAutoAward(Long userId);
}
