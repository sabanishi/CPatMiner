package jp.ac.titech.c.se.halrepair.atomicastchangemining.llm;

import jp.ac.titech.c.se.halrepair.atomicastchangemining.change.CASTNode;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.change.NormalizeType;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.groum.GROUMNode;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.mining.Fragment;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.mining.Pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PatternValidator {
    final static String systemMessage = "以下のパターンが妥当か判断して";

    private PatternValidator(){
    }

    public static boolean isPatternValid(Pattern pattern, HashSet<Pattern> dissolvedPatterns) {
        StringBuilder sb = new StringBuilder();

        Fragment representative = pattern.getRepresentative();
        sb.append("Pattern");
        String beforePattern = representative.getGraph().getNormalizedText(true);
        sb.append("\nBefore Change:\n").append(beforePattern);
        String afterPattern = representative.getGraph().getNormalizedText(false);
        sb.append("\nAfter Change:\n").append(afterPattern);

        HashSet<Fragment> fragments = pattern.getFragments();
        for(Fragment fragment : fragments){
            sb.append("\n\nExample Change:");
            String beforeFragment = fragment.getGraph().getRawText(true);
            sb.append("\nBefore Change:\n").append(beforeFragment);
            String afterFragment = fragment.getGraph().getRawText(false);
            sb.append("\nAfter Change:\n").append(afterFragment);
        }

        HashMap<GROUMNode, Boolean> canNormalizeMap = new HashMap<>();
        for(Fragment f : fragments){
            for(GROUMNode node :f.getNodes()){
                if(node.getIsNormalized()){
                    // 変数正規化は無視する
                    if(node.getNormalizeType() ==NormalizeType.Variable){
                        continue;
                    }

                    canNormalizeMap.put(node, true);
                }
            }
        }

        // TODO: fragmets内にNormalizedIdsの値が異なるやつがある場合はエラーを出す

        //System.out.println("Send Message");
        //System.out.println(sb.toString());

        String userMessage = sb.toString();
        //String result = LLMUser.send(systemMessage, userMessage, 0.2f);

        //System.out.println("Receive Message");
        //System.out.println(result);

        // 正規化が妥当でないノードにフラグを立てる
        boolean tmpFlag = false;
        for(GROUMNode node : canNormalizeMap.keySet()){
            if(tmpFlag){
                canNormalizeMap.put(node, false);
            }
        }

        for(GROUMNode node : canNormalizeMap.keySet()){
            if(!canNormalizeMap.get(node)){
                node.setIsNormalizeValid(false);
            }
        }

        // パターンを分解する
        HashMap<String, Pattern> dissolvedMap = new HashMap<>();
        for(Fragment f : fragments){
            String text = f.getGraph().makeEqualableKey();
            Pattern p = dissolvedMap.get(text);
            if(p == null){
                HashSet<Fragment> fs = new HashSet<>();
                fs.add(f);
                p = new Pattern(fs,1);
                dissolvedMap.put(text, p);
            }else{
                p.getFragments().add(f);
                p.setFreq(p.getFreq() + 1);
            }
        }

        dissolvedPatterns.addAll(dissolvedMap.values());

        return tmpFlag;
    }
}
