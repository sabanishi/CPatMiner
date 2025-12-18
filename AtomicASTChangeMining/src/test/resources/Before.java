public class A{
    // 1
    public int hoga(){
        String name = "hoge";
        assertEquals(name, "");
    }

    public int hoga(){
        String name = "hoge";
        assertThat(name).isEmpty();
    }


    // 2
    public int fuga(){
        String place = "Tokyo";
        assertEquals(place, "");
    }

    public int fuga(){
        String place = "Tokyo";
        assertThat(place).isEmpty();
    }


    // 3
    public int poyo(){
        String text = "poyo";
        assertEquals(text, "poyo");
    }

    public int poyo(){
        String text = "poyo";
        assertThat(text).isEmpty();
    }
}