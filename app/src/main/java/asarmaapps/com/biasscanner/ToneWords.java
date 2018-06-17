package asarmaapps.com.biasscanner;

public class ToneWords {
    private String[] positiveToneWords;
    private String[] negativeToneWords;
    private String[] neutralToneWords;
    private final int MAG = 5;

    public ToneWords(){
        setPositiveToneWords();
        setNegativeToneWords();
        setNeutralToneWords();
    }

    private void setPositiveToneWords(){
        String s = "Whimsical Lighthearted Convivial Optimistic Compassionate Sympathetic Benevolent Jovial Felicitous Carefree Exuberant Ecstatic Exhilarated Festive Contentment Affable Serene Sanguine Reverent Amicable";
        positiveToneWords = s.split(" ");
    }
    private void setNegativeToneWords(){
        String s = "enraged furious Melancholy Disgruntled discontented dissatisfied Lugubrious mournful sorrowful Disparaging sarcastic critical Inflamed irate provoked condescending Menacing ominous Hostile malevolent Enigmatic puzzling Bleak desolate lifeless downcast sorrowful Morose sullen gloomy Dismal dull barren";
        negativeToneWords = s.split(" ");
    }
    private void setNeutralToneWords(){
        String s = "Indifferent impersonal emotionless certain; assured Composed calm detached Sincere truthful straightforward comfortable; alluring Taciturn reserved subdued Uncertain Apprehensive";
        neutralToneWords = s.split(" ");
    }

    public String getWords(String type){
        switch (type){
            case "pos": return getRandom(positiveToneWords);
            case "neut": return getRandom(neutralToneWords);
            case "neg": return getRandom(negativeToneWords);
        }
        return "";
    }
    private String getRandom(String[] arr){
        String s = "";
        for(int i=0; i<MAG; i++){
            int index;
            do {
                index = (int) (Math.random() * arr.length);
            }while (s.contains(arr[index]));
            s+= arr[index] + ", ";
        }
        return s.toUpperCase();
    }
}
