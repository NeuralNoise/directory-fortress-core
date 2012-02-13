/*
 * Copyright (c) 2009-2011. Joshua Tree Software, LLC.  All Rights Reserved.
 */

package com.jts.fortress.pwpolicy.openldap;

import com.jts.fortress.SecurityException;

import com.jts.fortress.ValidationException;
import com.jts.fortress.constants.GlobalErrIds;
import com.jts.fortress.constants.GlobalIds;
import com.jts.fortress.pwpolicy.PswdPolicy;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.Set;


/**
 * Process module for the OpenLDAP Password Policy entity.  This class performs data validations and error mapping.
 * It is typically called by internal Fortress manager class {@link com.jts.fortress.pwpolicy.PswdPolicyMgrImpl} but also
 * needed by {@link com.jts.fortress.rbac.UserP#validate(com.jts.fortress.rbac.User, boolean)}
 * This class is not intended to be used by external programs.  This class will accept Fortress entity, {@link com.jts.fortress.pwpolicy.PswdPolicy}, on its
 * methods, validate contents and forward on to it's corresponding DAO class {@link PolicyDAO}.
 * <p/>
 * Class will throw {@link com.jts.fortress.SecurityException} to caller in the event of security policy, data constraint violation or system
 * error internal to DAO object. This class will forward DAO exceptions ({@link com.jts.fortress.FinderException},
 * {@link com.jts.fortress.CreateException},{@link com.jts.fortress.UpdateException},{@link com.jts.fortress.RemoveException}),
 * or {@link com.jts.fortress.ValidationException} as {@link com.jts.fortress.SecurityException}s with appropriate
 * error id from {@link com.jts.fortress.constants.GlobalErrIds}.
 * <p/>
 * This object uses one synchronized data sets {@link #policySet} but is thread safe.
 * <p/>

 *
 * @author smckinn
 * @created September 18, 2010
 */
public class PolicyP
{
    private static final String OCLS_NM = PolicyP.class.getName();
    private static final Logger log = Logger.getLogger(OCLS_NM);
    // This is 5 years duration in seconds:
    private static final int MAX_AGE = 157680000;

    // DAO class for ol pw policy data sets must be initialized before the other statics:
    private static final PolicyDAO olDao = new PolicyDAO();
    // static field holds the list of names for all valid pw policies in effect:
    private static Set<String> policySet = loadPolicySet();
    // this field is used to synchronize access to the above static data set:
    private static final Object policySetSynchLock = new Object();
    private static final int MIN_PW_LEN = 20;
    private static final int MAX_FAILURE = 100;
    private static final int MAX_GRACE_COUNT = 10;
    private static final int MAX_HISTORY = 100;

    /**
     * This function uses a case insensitive search.
     *
     * @param name
     * @return
     */
    public boolean isValid(String name)
    {
        boolean result = false;
        if (policySet != null)
            result = policySet.contains(name);
        return result;
    }

    /**
     * This method will return the password policy entity to the caller.  This command is valid
     * if and only if the policy entry is present in the POLICIES data set.
     *
     * @param name String contains the name of the policy entity.
     * @return PswdPolicy entity returns fully populated with attributes.
     * @throws com.jts.fortress.SecurityException In the event policy entry not found, data validation or system error.
     */
    public PswdPolicy read(String name)
        throws SecurityException
    {
        // Call the finder method for the primary key.
        return olDao.getPolicy(name);
    }

    /**
     * This method will add a new policy entry to the POLICIES data set.  This command is valid
     * if and only if the policy entry is not already present in the POLICIES data set.
     *
     * @param policy Object contains the password policy attributes.
     * @throws com.jts.fortress.SecurityException In the event of data validation or system error.
     */
    public void add(PswdPolicy policy)
        throws SecurityException
    {
        validate(policy);
        olDao.create(policy);
        synchronized (policySetSynchLock)
        {
            if (policySet != null)
                policySet.add(policy.getName());
        }
    }


    /**
     * This method will update an exiting policy entry to the POLICIES data set.  This command is valid
     * if and only if the policy entry is already present in the POLICIES data set.
     *
     * @param policy Object must contain the name of the policy entity.  All non-null attributes will
     *               be updated.  null attributes will be ignored.
     * @throws com.jts.fortress.SecurityException In the event policy not found , data validation or system error.
     */
    public void update(PswdPolicy policy)
        throws SecurityException
    {
        validate(policy);
        olDao.update(policy);
    }


    /**
     * This method will delete exiting policy entry from the POLICIES data set.  This command is valid
     * if and only if the policy entry is already present in the POLICIES data set.  Existing users that
     * are assigned this policy will be removed from association.
     *
     * @param policy Object must contain the name of the policy entity.
     * @throws com.jts.fortress.SecurityException In the event policy entity not found or system error.
     */
    public void delete(PswdPolicy policy)
        throws SecurityException
    {
        olDao.remove(policy);
        synchronized (policySetSynchLock)
        {
            if (policySet != null)
                policySet.remove(policy.getName());
        }
    }


    /**
     * This method will return a list of all password policy entities that match a particular search string.
     * This command will return an empty list of no matching entries are found.
     *
     * @param searchVal String contains the leading chars of a policy entity.  This search is not case sensitive.
     * @return List<PswdPolicy> contains all matching password policy entities. If no records found this will be empty.
     * @throws com.jts.fortress.SecurityException In the event of data validation or system error.
     */
    public final List<PswdPolicy> search(String searchVal)
        throws SecurityException
    {

        return olDao.findPolicy(searchVal);
    }


    /**
     * Method will perform simple validations to ensure the integrity of the OpenLDAP Password Policy entity targeted for insertion
     * or updating in directory.  Data reasonability checks will be performed on all non-null attributes.
     *
     * @param policy contains data targeted for insertion or update.
     * @throws com.jts.fortress.SecurityException in the event of data validation error or DAO error on Org validation.
     */
    private void validate(PswdPolicy policy)
        throws ValidationException
    {
        int length = policy.getName().length();
        if (length < 1 || length > GlobalIds.PWPOLICY_NAME_LEN)
        {
            String error = OCLS_NM + ".validate policy name [" + policy.getName() + "] INVALID LENGTH [" + length + "]";
            log.error(error);
            throw new ValidationException(GlobalErrIds.PSWD_NAME_INVLD_LEN, error);
        }
        if (policy.getCheckQuality() != null)
        {
            try
            {
                if (policy.getCheckQuality() < 0 || policy.getCheckQuality() > 2)
                {
                    String error = OCLS_NM + ".validate policy name [" + policy.getName() + "] value checkQuality [" + policy.getCheckQuality() + "] INVALID INT VALUE";
                    log.error(error);
                    throw new ValidationException(GlobalErrIds.PSWD_QLTY_INVLD, error);
                }
            }
            catch (java.lang.NumberFormatException nfe)
            {
                String error = OCLS_NM + ".validate policy name [" + policy.getName() + "] value checkQuality [" + policy.getCheckQuality() + "] INVALID INT VALUE";
                log.error(error);
                throw new ValidationException(GlobalErrIds.PSWD_QLTY_INVLD, error);
            }
        }
        if (policy.getMaxAge() != null)
        {
            if (policy.getMaxAge() > MAX_AGE)
            {
                String error = OCLS_NM + ".validate policy name [" + policy.getName() + "] value maxAge [" + policy.getMaxAge() + "] INVALID INT VALUE";
                log.error(error);
                throw new ValidationException(GlobalErrIds.PSWD_MAXAGE_INVLD, error);
            }
        }
        if (policy.getMinAge() != null)
        {
            // policy.minAge
            if (policy.getMinAge() > MAX_AGE)
            {
                String error = OCLS_NM + ".validate policy name [" + policy.getName() + "] value minAge [" + policy.getMinAge() + "] INVALID INT VALUE";
                log.error(error);
                throw new ValidationException(GlobalErrIds.PSWD_MINAGE_INVLD, error);
            }
        }
        if (policy.getMinLength() != null)
        {
            if (policy.getMinLength() > MIN_PW_LEN)
            {
                String error = OCLS_NM + ".validate policy name [" + policy.getName() + "] value minLength [" + policy.getMinLength() + "] INVALID INT VALUE";
                log.error(error);
                throw new ValidationException(GlobalErrIds.PSWD_MINLEN_INVLD, error);
            }
        }
        if (policy.getFailureCountInterval() != null)
        {
            if (policy.getFailureCountInterval() > MAX_AGE)
            {
                String error = OCLS_NM + ".validate policy name [" + policy.getName() + "] value failureCountInterval [" + policy.getFailureCountInterval() + "] INVALID INT VALUE";
                log.error(error);
                throw new ValidationException(GlobalErrIds.PSWD_INTERVAL_INVLD, error);
            }
        }
        if (policy.getMaxFailure() != null)
        {
            if (policy.getMaxFailure() > MAX_FAILURE)
            {
                String error = OCLS_NM + ".validate policy name [" + policy.getName() + "] value maxFailure [" + policy.getMaxFailure() + "] INVALID INT VALUE";
                log.error(error);
                throw new ValidationException(GlobalErrIds.PSWD_MAXFAIL_INVLD, error);
            }
        }
        if (policy.getInHistory() != null)
        {
            if (policy.getInHistory() > MAX_HISTORY)
            {
                String error = OCLS_NM + ".validate policy name [" + policy.getName() + "] value inHistory [" + policy.getInHistory() + "] INVALID VALUE";
                log.error(error);
                throw new ValidationException(GlobalErrIds.PSWD_HISTORY_INVLD, error);
            }
        }
        if (policy.getGraceLoginLimit() != null)
        {
            if (policy.getGraceLoginLimit() > MAX_GRACE_COUNT)
            {
                String error = OCLS_NM + ".validate policy name [" + policy.getName() + "] value graceLoginLimit [" + policy.getGraceLoginLimit() + "] INVALID VALUE";
                log.error(error);
                throw new ValidationException(GlobalErrIds.PSWD_GRACE_INVLD, error);
            }
        }
        if (policy.getLockoutDuration() != null)
        {
            if (policy.getLockoutDuration() > MAX_AGE)
            {
                String error = OCLS_NM + ".validate policy name [" + policy.getName() + "] value lockoutDuration [" + policy.getLockoutDuration() + "] INVALID VALUE";
                log.error(error);
                throw new ValidationException(GlobalErrIds.PSWD_LOCKOUTDUR_INVLD, error);
            }
        }
        if (policy.getExpireWarning() != null)
        {
            if (policy.getExpireWarning() > MAX_AGE)
            {
                String error = OCLS_NM + ".validate policy name [" + policy.getName() + "] value expireWarning [" + policy.getExpireWarning() + "] INVALID VALUE";
                log.error(error);
                throw new ValidationException(GlobalErrIds.PSWD_EXPWARN_INVLD, error);
            }
        }
    }


    /**
     * Load the cache with read only list of valid openldap policy names.
     *
     * @return Set of unique names.
     */
    private static Set<String> loadPolicySet()
    {
        Set<String> policySet = null;
        try
        {
            policySet = olDao.getPolicies();
        }
        catch (SecurityException se)
        {
            String warning = OCLS_NM + ".loadPolicySet static initializer caught SecurityException=" + se;
            log.info(warning);
        }
        return policySet;
    }
}