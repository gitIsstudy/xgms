package com.huel.xgms.app.user.dao;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.Result;
import org.jfaster.mango.annotation.Results;

/**
 * @author admin
 * @date 2018/3/4
 */
@DB(
        table = "t_xgms_user"
)
@Results({
        @Result(column = "c_id", property = "id"),
        @Result(column = "c_name", property = "userName"),
        @Result(column = "c_hobby", property = "hobby"),
        @Result(column = "c_preview_id", property = "previewId"),
        @Result(column = "c_file_id", property = "fileId"),
        @Result(column = "c_sex", property = "sex"),
        @Result(column = "d_birthday", property = "birthday"),
        @Result(column = "c_brief", property = "brief"),
        @Result(column = "c_school", property = "school"),
        @Result(column = "c_delete_flag", property = "deleteFlag"),
        @Result(column = "n_create_time", property = "createTime"),
        @Result(column = "n_update_time", property = "updateTime"),
        @Result(column = "n_age", property = "age"),
        @Result(column = "c_phone", property = "phone"),
        @Result(column = "c_auth", property = "auth"),
        @Result(column = "c_account_name", property = "accountName"),
        @Result(column = "c_pwd", property = "pwd")
})
public interface IUserDao {

    String COLUMNS = "c_name, c_hobby, c_preview_id, c_file_id, c_sex, d_birthday, c_brief, c_school, c_delete_flag, "
            + "n_create_time, n_update_time, c_phone, c_auth, c_account_name, c_pwd, n_age";
    String ALL_COLUMNS = "c_id, " + COLUMNS;

}
