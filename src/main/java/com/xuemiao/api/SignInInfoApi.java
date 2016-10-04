package com.xuemiao.api;

import com.xuemiao.api.Json.AbsenceReasonJson;
import com.xuemiao.api.Json.IdPasswordJson;
import com.xuemiao.api.Json.SignInActionJson;
import com.xuemiao.exception.IdNotExistException;
import com.xuemiao.exception.PasswordErrorException;
import com.xuemiao.exception.SignInOrderException;
import com.xuemiao.model.pdm.AbsenceEntity;
import com.xuemiao.model.pdm.SignInInfoEntity;
import com.xuemiao.model.pdm.StudentIdAndOperDateKey;
import com.xuemiao.model.pdm.SysAdminEntity;
import com.xuemiao.model.repository.AbsenceRepository;
import com.xuemiao.model.repository.SignInInfoRepository;
import com.xuemiao.model.repository.SysAdminRepository;
import com.xuemiao.service.AdminValidationService;
import com.xuemiao.service.CookieValidationService;
import com.xuemiao.utils.DateUtils;
import com.xuemiao.utils.PasswordUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Created by dzj on 9/30/2016.
 */
@Component
@Path("/sign_in_info_api")
public class SignInInfoApi {
    private final String cookiePath = "/api/sign_in_info_api";
    @Autowired
    SignInInfoRepository signInInfoRepository;
    @Autowired
    AdminValidationService adminValidationService;
    @Autowired
    CookieValidationService cookieValidationService;
    @Autowired
    AbsenceRepository absenceRepository;
    @Autowired
    SysAdminRepository sysAdminRepository;
    @Value("${admin.cookie.token.age}")
    int adminCookieAge;

    @POST
    @Path("/test")
    public Response testCookie(@CookieParam("token") String tokenString) {
        Response loginResponse = cookieValidationService.checkTokenCookie(tokenString, 1);
        if (loginResponse != null) {
            return loginResponse;
        }
        return Response.ok().entity(tokenString).build();
    }

    private NewCookie getCookie(Long id) {
        return cookieValidationService.getTokenCookie(id, cookiePath, adminCookieAge);
    }

    private NewCookie refreshCookie(String tokenString) {
        return cookieValidationService.refreshCookie(tokenString, cookiePath, adminCookieAge);
    }

    @POST
    @Path("/admin/validation")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response adminValidation(IdPasswordJson idPasswordJson)
            throws IdNotExistException, PasswordErrorException {
        adminValidationService.testPassword(idPasswordJson.getId(), idPasswordJson.getPassword1(), 1);
        System.out.println("XXX:"+adminCookieAge);
        return Response.ok().cookie(getCookie(idPasswordJson.getId())).build();
    }

    @PUT
    @Path("/admin/password_update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response adminPasswordUpdate(IdPasswordJson idPasswordJson)
            throws IdNotExistException, PasswordErrorException {
        adminValidationService.testPassword(idPasswordJson.getId(), idPasswordJson.getPassword1(), 1);
        SysAdminEntity sysAdminEntity = sysAdminRepository.findOne(idPasswordJson.getId());
        sysAdminEntity.setPasswordSalted(PasswordUtils.createPasswordHash(idPasswordJson.getPassword2()));
        sysAdminRepository.save(sysAdminEntity);
        return Response.ok().build();
    }

    @DELETE
    @Path("/admin/logout")
    public Response adminLogout(@CookieParam("token") String tokenString) {
        cookieValidationService.deleteCookieByToken(tokenString);
        return Response.ok().build();
    }

    @POST
    @Path("/sign_in_info/addition")
    public Response addSignIn(SignInActionJson signInActionJson,
                              @CookieParam("token")String tokenString)
            throws SignInOrderException {
        Response loginResponse = cookieValidationService.checkTokenCookie(tokenString, 1);
        if (loginResponse != null) {
            return loginResponse;
        }

        StudentIdAndOperDateKey studentIdAndOperDateKey = new StudentIdAndOperDateKey();
        studentIdAndOperDateKey.setStudentId(signInActionJson.getStudentId());
        studentIdAndOperDateKey.setOperDate(signInActionJson.getOperDate());
        SignInInfoEntity signInInfoEntity = signInInfoRepository.findOne(studentIdAndOperDateKey);
        Timestamp now = new Timestamp(DateTime.now().getMillis());
        switch (signInActionJson.getSignInOrder()) {
            case 1:
                signInInfoEntity.setStartMorning(now);
                break;
            case 2:
                signInInfoEntity.setEndMorning(now);
                break;
            case 3:
                signInInfoEntity.setStartAfternoon(now);
                break;
            case 4:
                signInInfoEntity.setEndAfternoon(now);
                break;
            case 5:
                signInInfoEntity.setStartNight(now);
                break;
            case 6:
                signInInfoEntity.setEndNight(now);
                break;
            default:
                throw new SignInOrderException();
        }
        signInInfoRepository.save(signInInfoEntity);

        return Response.ok().cookie(refreshCookie(tokenString)).build();
    }

    @POST
    @Path("/absences/addition")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addStudentAbsence(AbsenceReasonJson absenceReasonJson,
                                      @CookieParam("token")String tokenString) {
        Response loginResponse = cookieValidationService.checkTokenCookie(tokenString, 1);
        if (loginResponse != null) {
            return loginResponse;
        }

        StudentIdAndOperDateKey studentIdAndOperDateKey = new StudentIdAndOperDateKey();
        studentIdAndOperDateKey.setStudentId(absenceReasonJson.getStudentId());
        studentIdAndOperDateKey.setOperDate(absenceReasonJson.getOperDate());
        AbsenceEntity originAbsenceEntity = absenceRepository.findOne(studentIdAndOperDateKey);
        if(originAbsenceEntity==null){
            AbsenceEntity absenceEntity = new AbsenceEntity();
            absenceEntity.setStudentId(absenceReasonJson.getStudentId());
            absenceEntity.setOperDate(absenceReasonJson.getOperDate());
            absenceEntity.setAbsenceReason(absenceReasonJson.getAbsenceReason());
            absenceEntity.setStartAbsence(DateUtils.adjustYearMonthDay(absenceReasonJson.getStartAbsence()));
            absenceEntity.setEndAbsence(DateUtils.adjustYearMonthDay(absenceReasonJson.getEndAbsence()));
            absenceRepository.save(absenceEntity);
        }
        else{
            originAbsenceEntity.setAbsenceReason(absenceReasonJson.getAbsenceReason());
            originAbsenceEntity.setStartAbsence(DateUtils.adjustYearMonthDay(absenceReasonJson.getStartAbsence()));
            originAbsenceEntity.setEndAbsence(DateUtils.adjustYearMonthDay(absenceReasonJson.getEndAbsence()));
            absenceRepository.save(originAbsenceEntity);
        }
        return Response.ok().cookie(refreshCookie(tokenString)).build();
    }

}