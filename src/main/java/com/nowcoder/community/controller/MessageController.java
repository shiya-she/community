package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sc
 * @date 2023-06-15下午 11:14
 */
@Controller
@RequestMapping(path = "/letter")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final HostHolder hostHolder;
    private final UserService userService;

    @GetMapping("/list")
    @LoginRequired
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationsCount(user.getId()));
        List<Message> conversationList =
                messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = Optional.ofNullable(conversationList).stream()
                .flatMap(List::stream)
                .map(conversation -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("conversation", conversation);
                    map.put("letterCount", messageService.findLetterCount(conversation.getConversationId()));
                    map.put("unreadCount", messageService.findLetterUnReadCount(user.getId(), conversation.getConversationId()));
                    int targetId = user.getId() == conversation.getFromId() ? conversation.getToId() : conversation.getFromId();
                    map.put("target", userService.findUserById(targetId));
                    return map;
                }).collect(Collectors.toList());
        model.addAttribute("conversations", conversations);
        //查询未读消息数量
        int letterUnReadCount = messageService.findLetterUnReadCount(user.getId(), null);
        model.addAttribute("letterUnReadCount", letterUnReadCount);
        return "/site/letter";
    }

    @GetMapping(path = "/detail/{conversationId}")
    @LoginRequired
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page) {

        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Object> letters = Optional.ofNullable(letterList)
                .stream()
                .flatMap(List::stream)
                .map(message -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("letter", message);
                    map.put("formUser", userService.findUserById(message.getFromId()));
                    return map;
                }).collect(Collectors.toList());
        model.addAttribute("letters", letters);
        model.addAttribute("target", getLetterTarget(conversationId));

        List<Integer> ids = getLetterIds(letterList);

        Optional.ofNullable(ids)
                .filter(list -> !list.isEmpty())
                .ifPresent(messageService::readMessage);
        return "/site/letter-detail";
    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        return Optional.ofNullable(letterList).stream()
                .flatMap(List::stream)
                .filter(message -> hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0)
                .map(Message::getId)
                .collect(Collectors.toList());
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int i = Integer.parseInt(ids[0]);
        int j = Integer.parseInt(ids[1]);
        if (hostHolder.getUser().getId() == i) {
            return userService.findUserById(j);
        }
        return userService.findUserById(i);
    }

    @PostMapping(path = "/send")
    @ResponseBody
    @LoginRequired
    public String sendLetter(String toName, String content) {
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在！");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addNessage(message);
        return CommunityUtil.getJSONString(0);
    }
    @PostMapping(path = "/del")
    @LoginRequired
    @ResponseBody
    public  String deleteMessage(int messageId){
        User user = hostHolder.getUser();
        Message message = messageService.findMessageById(messageId);
        if (user .getId()!=message.getFromId()){
            return CommunityUtil.getJSONString(401,"无法删除此私信！");
        }
        messageService.deleteMessage(messageId);
        return CommunityUtil.getJSONString(200,"删除成功!");
    }
}
