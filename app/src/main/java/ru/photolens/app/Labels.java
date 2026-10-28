package ru.photolens.app;

import java.util.*;
public final class Labels {
    private static final Map<String,String> RU=new HashMap<>();
    static {
        String[][] values={
            {"Cat","Кошка"},{"Dog","Собака"},{"Bird","Птица"},{"Fish","Рыба"},{"Horse","Лошадь"},
            {"Animal","Животное"},{"Pet","Домашний питомец"},{"Rabbit","Кролик"},{"Butterfly","Бабочка"},
            {"Insect","Насекомое"},{"Reptile","Рептилия"},{"Snake","Змея"},{"Elephant","Слон"},
            {"Bear","Медведь"},{"Wildlife","Дикая природа"},{"Flower","Цветок"},{"Plant","Растение"},
            {"Tree","Дерево"},{"Forest","Лес"},{"Grass","Трава"},{"Sky","Небо"},{"Cloud","Облако"},
            {"Mountain","Гора"},{"Beach","Пляж"},{"Sea","Море"},{"Ocean","Океан"},{"Lake","Озеро"},
            {"River","Река"},{"Water","Вода"},{"Snow","Снег"},{"Sunset","Закат"},{"Nature","Природа"},
            {"Car","Автомобиль"},{"Vehicle","Транспорт"},{"Bicycle","Велосипед"},{"Motorcycle","Мотоцикл"},
            {"Airplane","Самолёт"},{"Train","Поезд"},{"Boat","Лодка"},{"Bus","Автобус"},{"Truck","Грузовик"},
            {"Food","Еда"},{"Fruit","Фрукты"},{"Vegetable","Овощи"},{"Pizza","Пицца"},{"Bread","Хлеб"},
            {"Cake","Торт"},{"Dessert","Десерт"},{"Coffee","Кофе"},{"Drink","Напиток"},{"Apple","Яблоко"},
            {"Banana","Банан"},{"Cheeseburger","Чизбургер"},{"Sushi","Суши"},{"Tableware","Посуда"},
            {"Person","Человек"},{"People","Люди"},{"Human","Человек"},{"Selfie","Селфи"},{"Smile","Улыбка"},
            {"Face","Лицо"},{"Crowd","Толпа"},{"Baby","Младенец"},{"Child","Ребёнок"},
            {"Building","Здание"},{"House","Дом"},{"Architecture","Архитектура"},{"City","Город"},
            {"Street","Улица"},{"Road","Дорога"},{"Bridge","Мост"},{"Room","Комната"},
            {"Furniture","Мебель"},{"Chair","Стул"},{"Table","Стол"},{"Bed","Кровать"},{"Couch","Диван"},
            {"Computer","Компьютер"},{"Laptop","Ноутбук"},{"Keyboard","Клавиатура"},{"Mobile phone","Мобильный телефон"},
            {"Phone","Телефон"},{"Television","Телевизор"},{"Monitor","Монитор"},{"Screen","Экран"},
            {"Book","Книга"},{"Toy","Игрушка"},{"Statue","Статуя"},{"Clock","Часы"},{"Ball","Мяч"},
            {"Clothing","Одежда"},{"Shoe","Обувь"},{"Hat","Шляпа"},{"Glasses","Очки"},
            {"Sports","Спорт"},{"Stadium","Стадион"},{"Soccer","Футбол"},{"Basketball","Баскетбол"},
            {"Swimming","Плавание"},{"Dancing","Танец"},{"Musical instrument","Музыкальный инструмент"},
            {"Guitar","Гитара"},{"Piano","Пианино"},{"Art","Искусство"},{"Painting","Картина"},
            {"Leisure","Отдых"},{"Event","Событие"},{"Landscape","Пейзаж"},{"Night","Ночь"},
            {"Cattle","Крупный рогатый скот"},{"Dairy","Молочные продукты"},{"Cuisine","Кухня"},
            {"Carnivore","Хищник"},{"Mammal","Млекопитающее"},{"Terrestrial animal","Наземное животное"},
            {"Fur","Шерсть"},{"Whiskers","Усы животного"},{"Snout","Морда"},{"Ear","Ухо"},
            {"Plant stem","Стебель"},{"Leaf","Лист"},{"Wood","Древесина"},{"Garden","Сад"},
            {"Table","Стол"},{"Fast food","Фастфуд"},{"Meal","Блюдо"},{"Baked goods","Выпечка"}
        };
        for(String[] pair:values)RU.put(pair[0].toLowerCase(Locale.ROOT),pair[1]);
    }
    private Labels(){}
    public static String russian(String value){return RU.getOrDefault(value.toLowerCase(Locale.ROOT),value);}
    public static final class Prediction {
        public final String label; public final float confidence;
        public Prediction(String label,float confidence){this.label=label;this.confidence=confidence;}
    }
    public static List<Prediction> top(List<Prediction> input,float threshold,int limit){
        if(limit<=0)return Collections.emptyList();
        List<Prediction> valid=new ArrayList<>();
        for(Prediction p:input)if(p.label!=null&&!p.label.trim().isEmpty()&&Float.isFinite(p.confidence)&&p.confidence>=threshold&&p.confidence<=1)valid.add(p);
        valid.sort((a,b)->Float.compare(b.confidence,a.confidence));
        List<Prediction> result=new ArrayList<>();Set<String> seen=new HashSet<>();
        for(Prediction p:valid)if(seen.add(p.label.toLowerCase(Locale.ROOT))){result.add(p);if(result.size()>=limit)break;}
        return result;
    }
}
