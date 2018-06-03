package com.openyogaland.denis.lines;

import android.support.annotation.NonNull;
import android.util.Log;

import org.junit.Test;

/**
 * Дана доска M x N клеток.
 * Клетки случайным образом заполнены шарами разных цветов (клетки могут быть и пустыми).
 * Всего есть K цветов.
 *
 * Напишите метод который находит на доске и удаляет все цепочки шариков одинакового цвета длиной
 * больше Q. Цепочка это Q и более шариков одинакового цвета расположенных на одной прямой по
 * вертикали, диагонали или горизонтали. Один шарик может быть частью нескольких цепочек (например,
 * если две цепочки пересекаются крестом или буквой "Г") - в этом случае должны быть удалены все
 * цепочки.
 *
 * Параметры M, N, K и Q вносятся в код как константы (система ввода этих параметров пользователем
 * не нужна). Доску до и после удаления шариков можно просто выводить в лог в каком-нибудь
 * читабельном виде (н-р, с цифрами обозначающими разные цвета.)
 * Программа должна отрабатывать без ошибок при любых заданных параметрах
 **/

public class ExampleUnitTest
{
  /**
   * constants
   */
  private static final String LOG_TAG = "TestLogic";
  
  private static final int WIDTH            = 8;  // M
  private static final int HEIGHT           = 8;  // N
  private static final int NUMBER_OF_COLORS = 3;  // K
  private static final int MIN_CHAIN_LENGTH = 3;  // Q
  
  /**
   * fields
   */
  private int[][] cell;
  private int[][] result;
  
  private int[] xLength, // длины цепочек по горизонтали,
                yLength, // длины цепочек по вертикали,
                sLength, // длины цепочек по слэш-диагонали
                bLength, // длины цепочек по бэкслэш-диагонали,
                xx0,     // x индексы старта горизонтальных цепочек
                yy0,     // y индексы старта вертикальных цепочек
                sx0,     // x индексы старта слэш-диагональных цепочек
                sy0,     // y индексы старта слэш-диагональных цепочек
                bx0,     // x индексы старта бэкслэш-диагональных цепочек
                by0;     // н индексы старта бэкслэш-диагональных цепочек
  
  /**
   * methods
   */
  @Test
  public void testRemoveColoredBalls()
  {
    checkParameters();
    initBoard();
    log("Исходный, случайно заполненный массив:");
    printBoardStateToLog(cell);
    log("Массив, содержащий найденные цепочки одинаковых элементов:");
    printBoardStateToLog(findChains());
    log("Исходный массив с вычтенными (заменены на 0) цепочками элементов:");
    printBoardStateToLog(removeChains());
  }
  
  private static void log(String message)
  {
    Log.d(LOG_TAG, message);
  }
  
  private void checkParameters() throws IllegalArgumentException
  {
    if(WIDTH <= 0)
    {
      log("Illegal argument: WIDTH should be positive int value");
      throw new IllegalArgumentException("WIDTH should be positive int value");
    }
    if(HEIGHT <= 0)
    {
      log("Illegal argument: HEIGHT should be positive int value");
      throw new IllegalArgumentException("HEIGHT should be positive int value");
    }
    if(NUMBER_OF_COLORS <= 0)
    {
      log("Illegal argument: NUMBER_OF_COLORS should be positive int value");
      throw new IllegalArgumentException("NUMBER_OF_COLORS should be positive int value");
    }
    if(MIN_CHAIN_LENGTH <= 0)
    {
      log("Illegal argument: MIN_CHAIN_LENGTH should be non-negative int value");
      throw new IllegalArgumentException("MIN_CHAIN_LENGTH should be non-negative int value");
    }
  }
  
  private void initBoard()
  {
    // инициируем массив целых чисел M х N, каждому числу соответствует свой цвет шарика
    cell = new int[WIDTH][HEIGHT];
    
    for(int xIndex = 0; xIndex < WIDTH; xIndex++)
    {
      for(int yIndex = 0; yIndex < HEIGHT; yIndex++)
      {
        /*
         присваиваем случайное целое значение (от 0 до K, включительно)
         каждому числу от 1 до K соответствует свой цвет шарика
         0 - нет шарика
         */
        cell[xIndex][yIndex] = (int) (Math.random() * (NUMBER_OF_COLORS + 1));
      }
    }
  }
  
  private void printBoardStateToLog(@NonNull int[][] cell)
  {
    // пробежать по всем горизонталям
    for(int yIndex = 0; yIndex < HEIGHT; yIndex++)
    {
      StringBuilder stringBuilder = new StringBuilder();
      String        rowToPrintToLog;
      
      // пробежать по всем вертикалям
      for(int xIndex = 0; xIndex < WIDTH; xIndex++)
      {
        stringBuilder.append(cell[xIndex][yIndex]).append(" ");
      }
      rowToPrintToLog = stringBuilder.toString();
      Log.d(LOG_TAG, rowToPrintToLog);
    }
  }
  
  private void clearAll()
  {
    for(int xIndex = 0; xIndex < WIDTH; xIndex++)
    {
      for(int yIndex = 0; yIndex < HEIGHT; yIndex++)
      {
        result[xIndex][yIndex] = 0;
      }
    }
  }
  
  private int[][] findChains()
  {
    result = new int[WIDTH][HEIGHT];
    
    if(MIN_CHAIN_LENGTH <= 1)
    { // тривиальный случай, удаляем все шарики с поля
      log("MIN_CHAIN_LENGTH <= 1");
      clearAll();
    }
    // случай, когда мы ищем цепочки и сохраняем их в новый массив
    else //noinspection ConstantConditions
      if(WIDTH >= MIN_CHAIN_LENGTH || HEIGHT >= MIN_CHAIN_LENGTH)
      {
        // инициализируем массивы для длин цепочек
        xLength = new int[HEIGHT];         // длина равна количеству горизонталей
        yLength = new int[WIDTH];          // длина равна количеству вертикалей
        sLength = new int[WIDTH + HEIGHT]; // длина равна количеству диагоналей
        bLength = new int[WIDTH + HEIGHT]; // длина равна количеству диагоналей
        
        // инициализируем массивы для индексов старта цепочек
        xx0 = new int[HEIGHT];         // длина равна количеству горизонталей
        yy0 = new int[WIDTH];          // длина равна количеству вертикалей
        sx0 = new int[WIDTH + HEIGHT]; // длина равна количеству диагоналей
        sy0 = new int[WIDTH + HEIGHT]; // длина равна количеству диагоналей
        bx0 = new int[WIDTH + HEIGHT]; // длина равна количеству диагоналей
        by0 = new int[WIDTH + HEIGHT]; // длина равна количеству диагоналей
        
        // пробегаем по значениям x
        int x = 0;
        while(x < WIDTH)
        {
          // пробегаем по значениям y
          int y = 0;
          while(y < HEIGHT)
          {
            // поиск горизонтальных цепочек
            findHorizontalChains(x, y);
            
            // поиск вертикальных цепочек
            findVerticalChains(x, y);
            
            // поиск слэш-диагональных цепочек
            findSlashDiagonalChains(x, y);
            
            findBackslashDiagonalChains(x, y);
            
            // следующий цикл
            y++;
          }
          // следующий цикл
          x++;
        }
      }
      else //noinspection ConstantConditions
        if((WIDTH < MIN_CHAIN_LENGTH) && (HEIGHT < MIN_CHAIN_LENGTH))
        { // тривиальный случай, на поле уже нет цепочек требуемой длины
          log("no chains");
          result = cell;
        }
    return result;
  }
  
  private void findHorizontalChains(int x, int y)
  {
    // xx - индекс соседнего элемента (следующего, или предыдущего)
    // индекс не должен выйти за границы горизонтали массива, но при этом нужно учесть
    // последний элемент
    int xx = (x == WIDTH - 1) ? (x - 1) : (x + 1);
    
    // если соседние горизонтальные элементы равны
    if(cell[x][y] == cell[xx][y])
    {
      // сохраняем стартовые индексы горизонтальных цепочек
      if(xLength[y] == 0)
      {
        xx0[y] = x;
      }
      // удлиняем горизонтальную цепочку (кроме случая последнего элемента)
      if(xx != x - 1)
      {
        xLength[y] = (xLength[y] == 0) ? 2 : (xLength[y] + 1);
      }
      
      // log("cell[x=" + x + "][y=" + y + "] = " + cell[x][y] + "; xx0[y=" + y + "] = " +
      // xx0[y] + "; " + "xLength[y=" + y + "] = " + xLength[y]);
      
      // записываем горизонтальную цепочку нужной длины в результат
      if(xLength[y] >= MIN_CHAIN_LENGTH)
      {
        for(int i = xx0[y]; i < xx0[y] + xLength[y]; i++)
        {
          result[i][y] = cell[i][y];
        }
      }
    }
    // если соседние горизонтальные элементы не равны
    if(cell[x][y] != cell[xx][y])
    {
      // начинаем новую горизонтальную цепочку
      xLength[y] = 1;
      // сохраняем стартовые индексы горизонтальных цепочек
      xx0[y] = xx;
    }
  }
  
  private void findVerticalChains(int x, int y)
  {
    {
      // yy - индекс соседнего элемента (следующего, или предыдущего)
      // индекс не должен выйти за границы вертикали массива, но при этом нужно учесть последний
      // элемент
      int yy = (y == HEIGHT - 1) ? (y - 1) : (y + 1);
      
      // если соседние вертикальные элементы равны
      if(cell[x][y] == cell[x][yy])
      {
        // сохраняем стартовые индексы вертикальных цепочек
        if(yLength[x] == 0)
        {
          yy0[x] = y;
        }
        // удлиняем вертикальную цепочку (кроме случая последнего элемента)
        if(yy != (y - 1))
        {
          yLength[x] = (yLength[x] == 0) ? 2 : (yLength[x] + 1);
        }
        
        // log("cell[x=" + x + "][y=" + y + "] = " + cell[x][y] + "; yy0[x=" + x + "] = " +
        // yy0[x] + "; " +
        //     "yLength[x=" + x + "] = " + yLength[x]);
        
        // записываем горизонтальную цепочку нужной длины в результат
        if(yLength[x] >= MIN_CHAIN_LENGTH)
        {
          // faster, but same as: for(int i = yy0[x]; i < yy0[x] + yLength[x]; i++)
          System.arraycopy(cell[x], yy0[x], result[x], yy0[x], yLength[x]);
        }
      }
      // если соседние горизонтальные элементы не равны
      if(cell[x][y] != cell[x][yy])
      {
        // начинаем новую горизонтальную цепочку
        yLength[x] = 1;
        // сохраняем стартовые индексы горизонтальных цепочек
        yy0[x] = yy;
      }
    }
  }
  
  private void findSlashDiagonalChains(int x, int y)
  {
    // xx - X-индекс соседнего слэш-диагонального элемента
    // yy - Y-индекс соседнего слэш-диагонального элемента
    // sd - индекс диагонального элемента в массиве слэш-диагоналей
    int sd = x + y;
    int xx = 0, yy = 0;
    if((x == WIDTH - 1 && y == HEIGHT - 1) ||                             // правый нижний
       (x == 0 && y == 0))
    {
      xx = x;
      yy = y;
    }         // левый верхний
    else if(x == WIDTH - 1)
    {
      xx = x - 1;
    }             // правый столбик
    else if(y == 0)
    {
      yy = y + 1;
    }             // верхний ряд
    else
    {
      xx = x + 1;
      yy = y - 1;
    } // остальные
    
    {
      // если соседние диагональные элементы равны
      if(cell[x][y] == cell[xx][yy])
      {
        // сохраняем стартовые индексы цепочек
        if(sLength[sd] == 0)
        {
          sx0[sd] = x;
          sy0[sd] = y;
        }
        // решаем, удлинять ли диагональную цепочку
        if(x == xx && y == yy)                 // правый нижний и левый верхний элементы
        {
          sLength[sd] = 1;
        }
        else if((xx == x - 1) || (yy == y + 1)) // правый столбик и верхний ряд
        {
          sLength[sd] = (sLength[sd] == 0) ? 1 : sLength[sd];
        }
        else                                    // остальные элементы
        {
          sLength[sd] = (sLength[sd] == 0) ? 2 : (sLength[sd] + 1);
        }
        
        //log("cell[x=" + x + "][y=" + y + "] = " + cell[x][y] + "; sx0[sd=" + sd + "] = " +
        //    sx0[sd] + "; sy0[sd=" + sd + "] = " + sy0[sd]+ "; sLength[sd=" + sd + "] = " +
        //    sLength[sd]);
        
        // записываем цепочку нужной длины в результат
        if(sLength[sd] >= MIN_CHAIN_LENGTH)
        {
          // log("sLength[sd="+sd+"]="+sLength[sd]+" >= MIN_CHAIN_LENGTH="+MIN_CHAIN_LENGTH);
          
          for(int i = sx0[sd], j = sy0[sd];
              (i < sx0[sd] + sLength[sd]) && (j > sy0[sd] - sLength[sd]); i++, j--)
          {
            // log("i=" + i + "; j=" + j);
            result[i][j] = cell[i][j];
          }
        }
      }
      // если соседние диагональные элементы не равны
      if(cell[x][y] != cell[xx][yy])
      {
        // начинаем новую цепочку
        sLength[sd] = 1;
        // сохраняем стартовые индексы цепочек
        sx0[sd] = xx;
        sy0[sd] = yy;
      }
    }
  }
  
  private void findBackslashDiagonalChains(int x, int y)
  {
    // xx - X-индекс соседнего бэкслэш-диагонального элемента
    // yy - Y-индекс соседнего бэкслэш-диагонального элемента
    // bd - индекс диагонального элемента в массиве бэкслэш-диагоналей
    int bd = x + (HEIGHT - 1) - y;
    
    int xx = 0, yy = 0;
    
    if((x == WIDTH - 1 && y == 0) ||                             // правый верхний
       (x == 0 && y == HEIGHT - 1))                      // левый нижний
    {
      xx = x;
      yy = y;
    }
    else if(x == WIDTH - 1)                                      // правый столбик
    {
      xx = x - 1;
    }
    else if(y == HEIGHT - 1)                                     // нижний ряд
    {
      yy = y - 1;
    }
    else                                                         // остальные
    {
      xx = x + 1;
      yy = y + 1;
    }
    
    // если соседние диагональные элементы равны
    if(cell[x][y] == cell[xx][yy])
    {
      // сохраняем стартовые индексы цепочек
      if(bLength[bd] == 0)
      {
        bx0[bd] = x;
        by0[bd] = y;
      }
      // решаем, удлинять ли диагональную цепочку
      if(x == xx && y == yy)                  // левый нижний и правый верхний элементы
      {
        bLength[bd] = 1;
      }
      else if((xx == x - 1) || (yy == y - 1)) // правый столбик и нижний ряд
      {
        bLength[bd] = (bLength[bd] == 0) ? 1 : bLength[bd];
      }
      else                                    // остальные элементы
      {
        bLength[bd] = (bLength[bd] == 0) ? 2 : (bLength[bd] + 1);
      }
      
      //log("cell[x=" + x + "][y=" + y + "] = " + cell[x][y] + "; bx0[bd=" + bd + "] = " +
      //    bx0[bd] + "; by0[bd=" + bd + "] = " + by0[bd]+ "; bLength[bd=" + bd + "] = " +
      //    bLength[bd]);
      
      // записываем цепочку нужной длины в результат
      if(bLength[bd] >= MIN_CHAIN_LENGTH)
      {
        //log("bLength[bd="+bd+"]="+bLength[bd]+" >= MIN_CHAIN_LENGTH="+MIN_CHAIN_LENGTH);
        
        for(int i = bx0[bd], j = by0[bd];
            (i < bx0[bd] + bLength[bd]) && (j < by0[bd] + bLength[bd]); i++, j++)
        {
          //log("i=" + i + "; j=" + j);
          result[i][j] = cell[i][j];
        }
      }
    }
    // если соседние диагональные элементы не равны
    if(cell[x][y] != cell[xx][yy])
    {
      // начинаем новую цепочку
      bLength[bd] = 1;
      // сохраняем стартовые индексы цепочек
      bx0[bd] = xx;
      by0[bd] = yy;
    }
  }
  
  private int[][] removeChains()
  {
    for(int i = 0; i < WIDTH; i++)
    {
      for(int j = 0; j < HEIGHT; j++)
      {
        // если в массиве result есть ненулевое значение
        // то в массиве cell, наоборот, эти элементы должны быть очищены
        cell[i][j] = (result[i][j] == 0) ? cell[i][j] : 0;
      }
    }
    return cell;
  }
}