package com.nowcoder.community.util;


import lombok.Data;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 *@author sc
 *@date 2023-06-11 21:27:35
 */

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    /**
     * description:替换符
     */
    private  static final String  REPLACEMENT ="**";

    //根节点
    private final  TrieNode rootNode=new TrieNode();
    @PostConstruct
    public  void init() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))
        ){
            String keyWord;
            while ((keyWord =reader.readLine())!=null){
                this.addKeyword(keyWord);
            }
        } catch (IOException e) {
            logger.error(String.format("加载敏感词文件失败:%s", e.getMessage()));
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public  String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        TrieNode tempNode = this.rootNode;
        int begin=0;
        int position=0;
        StringBuilder sb = new StringBuilder();
        while(position<text.length()){
            char c = text.charAt(position);
            //跳过符号
            if ( isSymbol(c)) {
                //若指针1处于根节点，将此符号记录结果，让指针2向下走一步
                if(tempNode==rootNode){
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头还是总结指针3都向下走一步
                position++;
                continue;
            }
            //检查下级节点
            tempNode=tempNode.getSubNode(c);
            if(tempNode==null){
                //以begin开始的字符串不是敏感词
                sb.append(text.charAt(begin));
                position=++begin;
                //重新指向根节点
                tempNode=rootNode;
            }else if(tempNode.isKeywordEnd()){
                //发现敏感词，将begin-position字符串替换掉
                sb.append(REPLACEMENT);
                //进入下一个位置
                begin=++position;
                tempNode=rootNode;
            }else{
                //检查下一个字符
                position++;
            }
        }
        //将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    private boolean isSymbol(Character c) {
        //0x2E80~0x9ff 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c)&&(c<0x2E80||c>0x9FFF);
    }


    //将一个敏感词加入到前缀树中
    private void addKeyword( String keyWord) {
        TrieNode  tempNode = rootNode;
        for (int i = 0; i < keyWord.length(); i++) {
            char c = keyWord.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if(subNode==null){
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //指向子节点，进入下一轮循环
            tempNode=subNode;
            //设置结束的标识
            if (i==keyWord.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    //前缀树节点
    @Data
    private class TrieNode {
        //关键字结束标识
        private boolean isKeywordEnd = false;
        //子节点（key是下级节点字符，value是下级节点）
        private Map<Character, TrieNode> subNode = new HashMap<>();

        //添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNode.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNode.get(c);
        }
    }
}
