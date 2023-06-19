package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.HostHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page) {
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        List<Message> letterlist = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Object> letters = Optional.ofNullable(letterlist)
                .stream()
                .flatMap(List::stream)
                .map(message -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("letter", message);
                    map.put("formUser", userService.findUserById(message.getFromId()));
                    return map;
                }).collect(Collectors.toList());
        model.addAttribute("letters", letters);
        model.addAttribute("target",getLetterTarget(conversationId));
        return "/site/letter-detail";
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int i = Integer.parseInt(ids[0]);
        int j = Integer.parseInt(ids[1]);
        if (hostHolder.getUser().getId() == i) {
            return userService.findUserById(i);
        }
        return userService.findUserById(j);
    }
}
