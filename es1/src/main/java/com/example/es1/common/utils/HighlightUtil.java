package com.example.es1.common.utils;

import cn.hutool.core.util.StrUtil;
import com.example.es1.dto.KeywordHighlightVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class HighlightUtil {

    public List<String> tokenizeKeyword(String keyword) {
        List<String> tokens = new ArrayList<>();
        if (StrUtil.isBlank(keyword)) {
            return tokens;
        }

        String[] parts = keyword.trim().split("\\s+");
        for (String part : parts) {
            if (StrUtil.isNotBlank(part)) {
                tokens.add(part);
            }
        }

        if (tokens.isEmpty()) {
            tokens.add(keyword);
        }

        return tokens;
    }

    public KeywordHighlightVO findKeywordPosition(String text, String keyword, String fieldName) {
        KeywordHighlightVO result = new KeywordHighlightVO();
        result.setField(fieldName);
        result.setText(text);

        if (StrUtil.isBlank(text) || StrUtil.isBlank(keyword)) {
            result.setPositions(new ArrayList<>());
            return result;
        }

        List<KeywordHighlightVO.KeywordPosition> positions = new ArrayList<>();
        List<String> tokens = tokenizeKeyword(keyword);

        for (String token : tokens) {
            if (StrUtil.isBlank(token)) {
                continue;
            }

            String escapedToken = Pattern.quote(token);
            Pattern pattern = Pattern.compile(escapedToken, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                positions.add(new KeywordHighlightVO.KeywordPosition(token, matcher.start(), matcher.end()));
            }
        }

        positions = mergeOverlappingPositions(positions);
        result.setPositions(positions);

        return result;
    }

    public KeywordHighlightVO generatePreviewWithPosition(String text, String keyword, String fieldName, int previewLength) {
        if (StrUtil.isBlank(text)) {
            KeywordHighlightVO result = new KeywordHighlightVO();
            result.setField(fieldName);
            result.setText("");
            result.setPositions(new ArrayList<>());
            return result;
        }

        List<KeywordHighlightVO.KeywordPosition> allPositions = findKeywordPosition(text, keyword, fieldName).getPositions();

        if (allPositions.isEmpty()) {
            String preview = text.length() > previewLength ? text.substring(0, previewLength) + "..." : text;
            KeywordHighlightVO result = new KeywordHighlightVO();
            result.setField(fieldName);
            result.setText(preview);
            result.setPositions(new ArrayList<>());
            return result;
        }

        int firstPos = allPositions.get(0).getStart();
        int start = Math.max(0, firstPos - previewLength / 2);
        int end = Math.min(text.length(), start + previewLength);

        if (start > 0 && !Character.isWhitespace(text.charAt(start))) {
            while (start > 0 && !Character.isWhitespace(text.charAt(start))) {
                start--;
            }
            if (start > 0) start++;
        }

        String previewText = text.substring(start, end);
        if (end < text.length()) {
            previewText = previewText + "...";
        }

        List<KeywordHighlightVO.KeywordPosition> adjustedPositions = new ArrayList<>();
        for (KeywordHighlightVO.KeywordPosition pos : allPositions) {
            if (pos.getStart() >= start && pos.getEnd() <= end) {
                adjustedPositions.add(new KeywordHighlightVO.KeywordPosition(pos.getKeyword(), pos.getStart() - start, pos.getEnd() - start));
            }
        }

        KeywordHighlightVO result = new KeywordHighlightVO();
        result.setField(fieldName);
        result.setText(previewText);
        result.setPositions(adjustedPositions);

        return result;
    }

    private List<KeywordHighlightVO.KeywordPosition> mergeOverlappingPositions(List<KeywordHighlightVO.KeywordPosition> positions) {
        if (positions.size() <= 1) {
            return positions;
        }

        positions.sort((a, b) -> Integer.compare(a.getStart(), b.getStart()));

        List<KeywordHighlightVO.KeywordPosition> merged = new ArrayList<>();
        KeywordHighlightVO.KeywordPosition current = positions.get(0);

        for (int i = 1; i < positions.size(); i++) {
            KeywordHighlightVO.KeywordPosition next = positions.get(i);
            if (next.getStart() <= current.getEnd()) {
                String mergedKeyword = current.getKeyword() + next.getKeyword();
                current = new KeywordHighlightVO.KeywordPosition(mergedKeyword, current.getStart(), Math.max(current.getEnd(), next.getEnd()));
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        return merged;
    }
}
