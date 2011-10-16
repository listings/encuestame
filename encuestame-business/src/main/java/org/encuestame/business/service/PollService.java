/*
 ************************************************************************************
 * Copyright (C) 2001-2011 encuestame: system online surveys Copyright (C) 2010
 * encuestame Development Team.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.encuestame.business.service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.encuestame.core.service.imp.IPollService;
import org.encuestame.core.util.ConvertDomainBean;
import org.encuestame.core.util.EnMeUtils;
import org.encuestame.persistence.domain.Email;
import org.encuestame.persistence.domain.question.Question;
import org.encuestame.persistence.domain.security.UserAccount;
import org.encuestame.persistence.domain.survey.Poll;
import org.encuestame.persistence.domain.survey.PollFolder;
import org.encuestame.persistence.exception.EnMeExpcetion;
import org.encuestame.persistence.exception.EnMeNoResultsFoundException;
import org.encuestame.persistence.exception.EnMePollNotFoundException;
import org.encuestame.utils.MD5Utils;
import org.encuestame.utils.enums.CommentOptions;
import org.encuestame.utils.enums.NotificationEnum;
import org.encuestame.utils.enums.TypeSearch;
import org.encuestame.utils.enums.TypeSearchResult;
import org.encuestame.utils.json.FolderBean;
import org.encuestame.utils.json.QuestionBean;
import org.encuestame.utils.json.TweetPollBean;
import org.encuestame.utils.web.PollBean;
import org.encuestame.utils.web.UnitLists;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Poll Service.
 * @author Morales, Diana Paola paola AT encuestame.org
 * @since  April 01, 2010
 * @version $Id: $
 */
@Service
public class PollService extends AbstractSurveyService implements IPollService{

    /**
     * Log.
     */
    private Log log = LogFactory.getLog(this.getClass());


    /*
     * (non-Javadoc)
     * @see org.encuestame.core.service.imp.IPollService#filterPollByItemsByType(org.encuestame.utils.enums.TypeSearch, java.lang.String, java.lang.Integer, java.lang.Integer, org.encuestame.utils.enums.TypeSearchResult)
     */
    public List<PollBean> filterPollByItemsByType(final TypeSearch typeSearch,
            String keyword, Integer max, Integer start)
            throws EnMeNoResultsFoundException, EnMeExpcetion {
        final List<PollBean> list = new ArrayList<PollBean>();
        if (TypeSearch.KEYWORD.name().equals(typeSearch)) {
            list.addAll(this.searchPollByKeyword(keyword, max, start));
        } else if (TypeSearch.BYOWNER.name().equals(typeSearch)) {
            list.addAll(ConvertDomainBean.convertListToPollBean(getPollDao()
                    .findAllPollByEditorOwner(
                            getUserAccount(getUserPrincipalUsername()), max,
                            start)));
        }
//        } else if (TypeSearch.LASTDAY.name().equals(typeSearch)) {
//            list.addAll(this.searchTweetsPollsToday(getUserPrincipalUsername(),
//                    max, start));
//        } else if (TypeSearch.LASTWEEK.name().equals(typeSearch)) {
//            list.addAll(this.searchTweetsPollsLastWeek(
//                    getUserPrincipalUsername(), max, start));
//        } else if (TypeSearch.FAVOURITES.name().equals(typeSearch)) {
//            list.addAll(this.searchTweetsPollFavourites(
//                    getUserPrincipalUsername(), max, start));
//        } else if (TypeSearch.SCHEDULED.name().equals(typeSearch)) {
//
//            list.addAll(this.searchTweetsPollScheduled(
//                    getUserPrincipalUsername(), max, start));
//        } else {
//            list.addAll(this.getTweetsPollsByUserName(
//                    getUserPrincipalUsername(), max, start));
//        }
        return list;
    }

    /**
     * Create Poll.
     */
    public Poll createPoll(final String questionName, final String[] answers, final Boolean showResults,
        final String commentOption, final Boolean notification) throws EnMeExpcetion{
        final UserAccount user = getUserAccount(getUserPrincipalUsername());
        Assert.notNull(answers);
        Assert.notNull(user);
        Assert.notNull(questionName);
        final Poll pollDomain = new Poll();
        try {
            final QuestionBean questionBean = new QuestionBean();
            questionBean.setQuestionName(questionName);
            final Question question = createQuestion(questionBean, user);
            log.debug("question found : {"+question);
            log.debug("answers found : {"+answers.length);
            if (question == null) {
                throw new EnMeNoResultsFoundException("Question not valid");
            } else if (answers.length  == 0 ) {
                  throw new EnMeNoResultsFoundException("answers are required to create Poll");
            }
            else{
            //TODO: move hash to util.
            final String hashPoll = MD5Utils.md5(RandomStringUtils.randomAlphanumeric(500));
            final CommentOptions commentOpt = CommentOptions.getCommentOption(commentOption);
            pollDomain.setEditorOwner(user);
            pollDomain.setStartDate(Calendar.getInstance().getTime());
            pollDomain.setPollHash(hashPoll);
            pollDomain.setQuestion(question);
            pollDomain.setPollCompleted(Boolean.FALSE);
            pollDomain.setHits(EnMeUtils.HIT_DEFAULT);
            pollDomain.setRelevance(EnMeUtils.RATE_DEFAULT);
            pollDomain.setLikeVote(EnMeUtils.LIKE_DEFAULT);
            pollDomain.setDislikeVote(EnMeUtils.DISLIKE_DEFAULT);
            pollDomain.setNumbervotes(EnMeUtils.VOTE_MIN);
            pollDomain.setEditorOwner(user);
            pollDomain.setAccountItem(user.getAccount());
            pollDomain.setShowResults(showResults);
            pollDomain.setShowComments(commentOpt);
            pollDomain.setPublish(Boolean.TRUE);
            pollDomain.setNotifications(notification);
            pollDomain.setPublish(Boolean.TRUE);
            for (int row = 0; row < answers.length; row++) {
                 final String answersText = answers[row];
                 Assert.notNull(answersText);
                 if (answersText.isEmpty()) {
                     createAnswers(question, answersText.trim());
                 }
            }
            this.getPollDao().saveOrUpdate(pollDomain);
            }
        } catch (Exception e) {
            throw new EnMeExpcetion(e);
        }
        return pollDomain;
    }

    /**
     * Get Poll by Id.
     * @param pollId
     * @return
     */
     private Poll getPoll(final Long pollId){
         return this.getPollDao().getPollById(pollId);
     }

     /**
      * Remove Poll
      * @param pollId
      * @throws EnMeNoResultsFoundException
      */
    public void removePoll(final Long pollId) throws EnMeNoResultsFoundException{
        final Poll pollDomain = this.getPoll(pollId);
        if(pollDomain != null){
              getPollDao().delete(pollDomain);
          } else {
              throw new EnMeNoResultsFoundException("Poll not found");
          }
      }

    /**
     * Search Polls by Question keyword.
     * @param keywordQuestion
     * @param username
     * @param maxResults
     * @param start
     * @return
     * @throws EnMeExpcetion
     */
    public List<PollBean> searchPollByKeyword(final String keywordQuestion, final Integer maxResults,
        final Integer start) throws EnMeExpcetion{
        log.info("search keyword Poll  "+keywordQuestion);
        List<Poll> polls = new ArrayList<Poll>();
        if (keywordQuestion == null) {
            throw new EnMeExpcetion("keyword is mandatory");
        } else {
            polls = getPollDao().getPollsByQuestionKeyword(keywordQuestion,
                    getUserAccount(getUserPrincipalUsername()), maxResults, start);
        }
        log.info("search keyword polls size "+polls.size());
        return ConvertDomainBean.convertListToPollBean(polls);
       }

    /**
     * Search Polls by Folder Id.
     * @param folderId
     * @param username
     * @return
     * @throws EnMeNoResultsFoundException
     */
    public List<PollBean> searchPollsByFolder(final Long folderId, final String username) throws EnMeNoResultsFoundException{
        final PollFolder pollFolder = getPollDao().getPollFolderById(folderId);
        List<Poll> polls = new ArrayList<Poll>();
        if (pollFolder != null){
            polls = getPollDao().getPollsByPollFolderId(getUserAccount(getUserPrincipalUsername()), pollFolder);
        }
        log.info("search polls by folder size "+polls.size());
        return ConvertDomainBean.convertSetToPollBean(polls);
    }


    /**
     * List Poll ByUser.
     * @param currentUser currentUser
     * @return unitPoll
     * @throws EnMeNoResultsFoundException
     */

    public List<PollBean> listPollByUser(final Integer maxResults, final Integer start) throws EnMeNoResultsFoundException{
        final List<PollBean> unitPoll = new ArrayList<PollBean>();
        final List<Poll> polls = getPollDao().findAllPollByEditorOwner(getUserAccount(getUserPrincipalUsername()), maxResults, start);
         for (Poll poll : polls) {
             unitPoll.add(ConvertDomainBean.convertPollDomainToBean(poll));
        }
        return unitPoll;
    }

    /**
     * Get Polls by Folder.
     * @param folder
     * @param username
     * @return
     * @throws EnMeNoResultsFoundException
     */
    public List<PollBean> getPollsByFolder(final FolderBean folder) throws EnMeNoResultsFoundException{
        final List<Poll> polls = getPollDao().getPollsByPollFolder(getUserAccount(getUserPrincipalUsername()), getPollFolder(folder.getId()));
        return ConvertDomainBean.convertSetToPollBean(polls);
    }

    /**
     *
    */
    public void updateAnswersPoll( ){
    }

    /*
     * (non-Javadoc)
     * @see org.encuestame.core.service.imp.IPollService#updateQuestionPoll(java.lang.Long, org.encuestame.persistence.domain.question.Question)
     */
    public PollBean updateQuestionPoll(final Long pollId, final Question question)
            throws EnMeExpcetion {
          final Poll poll = this.getPollById(pollId);
          if(poll == null) {
              throw new EnMeNoResultsFoundException("Poll not found");
          }
          else{
              poll.setQuestion(question) ;
              getPollDao().saveOrUpdate(poll);
          }
           return ConvertDomainBean.convertPollDomainToBean(poll);

    }

    /**
     * Create url poll Access.
     * @param poll
     * @param domain
     * @param currentUser
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private String createUrlPollAccess(final Poll poll) {
        StringBuffer urlBuffer = new StringBuffer("/poll/");
        urlBuffer.append(poll.getPollHash());
        urlBuffer.append("/");
        urlBuffer.append(poll.getPollId());
        urlBuffer.append("/");
        urlBuffer.append(poll.getQuestion().getSlugQuestion());
        return urlBuffer.toString();
    }

    /*
     * (non-Javadoc)
     * @see org.encuestame.core.service.imp.IPollService#createPollNotification(org.encuestame.persistence.domain.survey.Poll)
     */
    public void createPollNotification(final Poll poll) throws EnMeNoResultsFoundException {
        createNotification(NotificationEnum.POLL_PUBLISHED,
                getMessageProperties("notification.poll.publish"),
                this.createUrlPollAccess(poll), false);
    }

    /*
     * (non-Javadoc)
     * @see org.encuestame.core.service.imp.IPollService#publicPollByList(org.encuestame.persistence.domain.survey.Poll, org.encuestame.utils.web.UnitLists)
     */
    @SuppressWarnings("unused")
    public void publicPollByList(final Poll poll, final UnitLists emailList) {
        final List<Email> emailsList = getEmailListsDao().findEmailsByListId(emailList.getId());
        final String urlPoll = this.createUrlPollAccess(poll);
        if(emailList !=null){
                 for (Email emails : emailsList) {
                   getMailService().send(emails.getEmail(),"New Poll", urlPoll);
                  }
         }
         else{
             log.warn("Not Found Emails in your EmailList");
        }
    }

    /**
     *
     * @param folderId
     * @return
     */
    public List<PollBean> getPollByFolderId(final Long folderId){
        return null;
    }

    /**
     * Retrieve Folder Poll.
     * @param username
     * @return
     * @throws EnMeNoResultsFoundException exception
     */
    public List<FolderBean> retrieveFolderPoll() throws EnMeNoResultsFoundException {
        final List<PollFolder> folders = getPollDao().getPollFolderByUserAccount(getUserAccount(getUserPrincipalUsername()));
        return ConvertDomainBean.convertListPollFolderToBean(folders);
    }

    /**
     * Create Poll Folder.
     * @param folderName
     * @param username
     * @return
     * @throws EnMeNoResultsFoundException
     */
    public FolderBean createPollFolder(final String folderName) throws EnMeNoResultsFoundException {
        final PollFolder pollFolder = new PollFolder();
        pollFolder.setCreatedBy(getUserAccount(getUserPrincipalUsername()));
        pollFolder.setCreatedAt(Calendar.getInstance().getTime());
        pollFolder.setFolderName(folderName);
        pollFolder.setUsers(getUserAccount(getUserPrincipalUsername()).getAccount());
        this.getPollDao().saveOrUpdate(pollFolder);
        return ConvertDomainBean.convertFolderToBeanFolder(pollFolder);
    }

    /**
     * Update FolderName.
     * @param folderId folder id
     * @param newFolderName folder name
     * @param username username
     * @return {@link FolderBean}
     * @throws EnMeNoResultsFoundException exception
     */
    public FolderBean updateFolderName(final Long folderId,
            final String newFolderName,
            final String username) throws EnMeNoResultsFoundException{
        final PollFolder folder = this.getPollFolder(folderId);
        if(folder == null){
            throw new EnMeNoResultsFoundException("poll folder not found");
        } else {
            folder.setFolderName(newFolderName);
            getPollDao().saveOrUpdate(folder);
        }
        return ConvertDomainBean.convertFolderToBeanFolder(folder);
    }

    /**
     * Get Poll Folder.
     * @param id
     * @return
     * @throws EnMeNoResultsFoundException
     */
    private PollFolder getPollFolder(final Long id) throws EnMeNoResultsFoundException{
        if(id == null){
             throw new EnMeNoResultsFoundException("poll folder id not found");
        }else {
        return this.getPollDao().getPollFolderById(id);
        }
    }

    /**
     * Remove PollFolder.
     * @param folderId
     * @throws EnMeNoResultsFoundException
     */
    public void removePollFolder(final Long folderId) throws EnMeNoResultsFoundException{
        final PollFolder folder = this.getPollFolder(folderId);
        if(folder != null){
            getPollDao().delete(folder);
        } else {
            throw new EnMeNoResultsFoundException("poll folder not found");
        }
    }

    public List<PollBean> listPollByUser(String currentUser)
            throws EnMeNoResultsFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Add poll to Folder.
     * @param folderId
     * @param username
     * @param pollId
     * @throws EnMeNoResultsFoundException
     */
    public void addPollToFolder(final Long folderId, final Long pollId)
                                throws EnMeNoResultsFoundException{
        final PollFolder pfolder = this.getPollFolderByFolderIdandUser(folderId, getUserAccount(getUserPrincipalUsername()));
        if (pfolder!=null) {
            final Poll poll = getPollDao().getPollById(pollId, getUserAccount(getUserPrincipalUsername()));
            if (poll == null){
                throw new EnMeNoResultsFoundException("TweetPoll not found");
             }
            poll.setPollFolder(pfolder);
            getPollDao().saveOrUpdate(poll);
        } else {
            throw new EnMeNoResultsFoundException("TweetPoll folder not found");
    }
}

    /**
     * Get Poll folder.
     * @param folderId
     * @param userId
     * @return
     */
    public PollFolder getPollFolderByFolderIdandUser(final Long folderId, final UserAccount userAccount){
        return this.getPollDao().getPollFolderByIdandUser(folderId, userAccount);
    }

    /**
     * Get polls by creation date.
     * @param userId
     * @param date
     * @param maxResults
     * @param star
     * @return
     * @throws EnMeNoResultsFoundException
     */
    public List<PollBean> getPollsbyDate(final Date date, final Integer maxResults,
            final Integer start) throws EnMeNoResultsFoundException{
        List<Poll> pollList = new ArrayList<Poll>();
        if (date !=null){
            pollList = getPollDao().getPollByUserIdDate(date, getUserAccount(getUserPrincipalUsername()), maxResults, start);
        }
        else{
            throw new EnMeNoResultsFoundException("Date not found");
        }
        return ConvertDomainBean.convertSetToPollBean(pollList);
    }

    /**
     * Get poll by id and user.
     * @param pollId
     * @param account
     * @return
     * @throws EnMeNoResultsFoundException
     */
     public Poll getPollById(final Long pollId, final UserAccount account) throws EnMeNoResultsFoundException{
        final Poll poll = getPollDao().getPollById(pollId, getUserAccount(getUserPrincipalUsername()));
        if (poll == null) {
            log.error("poll invalid with this id "+pollId+ " and username:{"+account);
            throw new EnMePollNotFoundException("poll invalid with this id "+pollId+ " and username:{"+account);
        }
        return poll;
    }

    /**
     * Get poll by id.
     * @param pollId
     * @return
     * @throws EnMePollNotFoundException

     */
    public Poll getPollById(final Long pollId) throws EnMePollNotFoundException{
        final Poll poll = getPollDao().getPollById(pollId);
        if (poll == null) {
            log.error("poll invalid with this id "+pollId);
            throw new EnMePollNotFoundException("poll invalid with this id "+pollId);
        }
        return poll;
    }

    /*
     * (non-Javadoc)
     * @see org.encuestame.business.service.imp.IPollService#getPollsByUserName(java.lang.String, java.lang.Integer, java.lang.Integer)
     */
    public List<PollBean> getPollsByUserName(
            final String username,
            final Integer maxResults,
            final Integer start) throws EnMeNoResultsFoundException{
        log.debug("Poll username "+username);
        final List<Poll> polls = getPollDao()
             .retrievePollsByUserId(getUserAccount(getUserPrincipalUsername()), maxResults, start);
         log.info("Polls size "+ polls.size());
         final List<PollBean> pollBean = ConvertDomainBean.convertSetToPollBean(polls);
        return pollBean;
    }

    /*
     * (non-Javadoc)
     * @see org.encuestame.core.service.imp.IPollService#getPolls(java.lang.Integer, java.lang.Integer, java.util.Date)
     */
    public List<Poll> getPollsByRange(final Integer maxResults,
            final Integer start, final Date range) {
        final List<Poll> polls = getPollDao().getPolls(
                maxResults, start, range);
        return polls;
    }
}
