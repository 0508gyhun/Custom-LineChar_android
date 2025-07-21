# Custom-LineChar_android
## 라이브러리를 사용한 LineChart에 대하여
  
-  안드로이드에 Line Chart를 구현하기 위해서는 MpAndroidLineChart 라는 라이브러리가 가장 유명하면서 많이 사용됩니다.
- 이 라이브러리를 사용하여 구현한 결과 추상화된 코드 내부의 코드가 Canvas와 drawText() 등의 함수로 이루어진 것을 확인하였습니다.

## 라이브러리를 사용하지 않은 Custom LineChart를 만들어보자.

<img width="650" height="400" alt="Image" src="https://github.com/user-attachments/assets/5d4663ae-e7d8-4240-a134-f2952c5f75f7" />

- 위와 같이 외부 라이브러리를 이용하면 구현하는 면에서 원리보다는 추상화된 설정을 입력하는 방식으로 더 간단하게 구현할 수 있습니다.  하지만 어떻게 추상화가 이루어진 몇개의 설정으로 line chart가 구현될 수 있는지를 학습하고자, 라이브러리를 지원하지 않는 커스텀 UI를 구현해야할 경우를 대비해 직접 디자인 해보았습니다.

- 커스텀 line chart의 구현은 전반적으로 다음과 같이 진행되었습니다.

  - 1.초기 색상, 여백 설정 및 데이터의 최대 최소값을 추출하여 그래프의 범위를 정합니다.
  - 2.컴포저블 객체인 canvas를 도화지 용도로 사용을 준비합니다..
  - 3.line 차트에 필요한 x축, y축, 그리드를 그립니다. 
  - 4.x축과 y축을 이루는 데이터에 표시를 먼저 해두고 각 위치에 연결된 선을 연결하여데이터 추이를 보여줍니다.
  - 5.x축과 떨어진 공간 만큼 영역을 채워 시각적인 표현을 합니다.


- 더 자세히 설명하겠습니다.
0.
<img width="773" height="788" alt="Image" src="https://github.com/user-attachments/assets/b838e84a-2a1f-475b-bebf-bec3de96de57" />

- 실행된 MainActivity의 코드입니다. 데이터는pair<String,Float> 타입으로 dummy data를 생성하였습니다.
- 화면은 전체를 차지하도록 구현하였습니다.

1.
<img width="650" height="400" alt="Image" src="https://github.com/user-attachments/assets/6cb3159f-d698-49a2-9ff7-7182816ae3b7" /> 

- val spacing - 화면 바닥에서 부터 x축과 y축을 얼마나 떨어뜨릴 건지 정하는 변수입니다.
- graphColor, transparentGraphColor 변수를 사용하여, 그래프 색상과 그래프를 채울 투명도가 적용된 색상을 적용합니다.
- upperValue,lowerValue 변수를 통하여 파라미터로 들어온 데이터의 최댓값,최솟값을 정합니다. plus 사용 이유는 너무 화면 끝에 닿지않게 하기 위한 여유 공간입니다.
- desity는 화면 밀도를 나타내는 변수이고, 화면 밀도가 변화될때 마다 텍스트 크기를 픽셀값으로 변환하기 위해 사용됩니다. 밀도에 변화가 있을때, 차트의 라벨 텍스트의 설정인 Paint 객체가 초기화 됩니다. (성능 최적화를 위해)

**알고 넘어가면 좋은 부분**
Jetpack Compose의 Recomposition과 remember의 역할
- 위에서 몇몇 변수에는 remember가 있고 없고의 차이가 있습니다.
- Jetpack Compose에서 UI를 업데이트하는 핵심 메커니즘인 재구성은, 컴포저블 함수가 읽고 있는 상태(state 객체, 매개변수, compositionLocal을 통한 환경값)가 변경되거나, 해당 컴포저블을 호출하는 상위 컴포저블이 재구성될 때 발생합니다.
-  이는 마치 화면의 일부가 변하면 해당 부분만 효율적으로 다시 그려지는 과정과 같습니다.
-  그러나 컴포저블이 재구성될 때마다 그 내부의 모든 코드 라인이 처음부터 다시 실행되므로, 이 과정에서 성능 저하가 발생할 수 있는 지점들이 생깁니다. 이를 방지하고 효율성을 최적화하기 위해 remember 함수가 사용됩니다.
- remember는 컴포저블이 재구성되더라도 특정 값을 기억하게 함으로써, 불필요한 계산이나 새로운 객체 생성을 방지합니다.
- 이는 크게 세 가지 경우에 유용하게 쓰입니다.
- 첫째, 데이터 목록에서 최대/최소값 계산이나 복잡한 UI 그리기 객체(Path, Paint 등) 생성과 같이 계산 비용이 많이 드는 작업의 결과를 기억하여 반복 계산을 막습니다.
- 둘째, Color.Red.copy(alpha = 0.5f)와 같이 값이 동일하더라도 매번 새로운 인스턴스를 생성하는 객체의 경우, 동일한 객체를 재사용함으로써 메모리 사용을 효율화하고 가비지 컬렉션(GC) 부담을 줄입니다. 
- 셋째, mutableStateOf(0)와 함께 사용되어 컴포저블의 생명 주기 동안 유지되어야 하는 동적인 상태 자체를 기억함으로써, 상태가 변경될 때마다 UI가 올바르게 업데이트되도록 합니다.
- 반면에 50f나 Color.Cyan과 같은 하드코딩된 상수 값이나, LocalDensity.current처럼 항상 최신 환경 값을 가져와야 하는 경우에는 remember를 사용하지 않아도 됩니다. 상수 값은 애초에 변하지 않으므로 기억할 필요가 없으며, 환경 값은 실시간 반영이 중요하기 때문입니다. 
- remember(key)와 같이 키를 명시적으로 지정하면, 해당 키의 값이 변경될 때만 remember 블록 내부의 계산이 다시 실행되도록 하여, data 변경으로 인해 컴포저블 전체가 재구성되더라도 density와 같은 특정 키가 변하지 않았다면 해당 remember 블록의 재계산을 건너뛰는 기능이 가능합니다.

2.
<img width="550" height="300" alt="Image" src="https://github.com/user-attachments/assets/4f898911-dbf4-4b7a-9313-9a4af074ec07" />
<img width="150" height="300" alt="Image" src="https://github.com/user-attachments/assets/0086798d-9aeb-4eae-a713-1e79b5ff4570" /> 
<img width="150" height="300" alt="Image" src="https://github.com/user-attachments/assets/98e81919-0203-4eb2-b158-610d2c9a8aeb" />


- Canvas 컴포저블 객체를 생성하고 그 안에서 x축 라벨을 그리는 작업을 합니다.
- Canvas에서 가장 먼저 알아야 할 점은 왼쪽 상단이 (0,0)이고, 오른쪽으로 갈수록 x 값이 증가하며, 아래로 향할수록 y 값이 증가한다는 점입니다.
- 변수 spacing은 x 축을 바닥에서 조금 떨어뜨릴 여유 공간이라고 생각하시면 됩니다.
- spacePerHour 변수는 x축의 라벨 간격을 전체 넓이에서 여유공간을 제거하고 데이터의 수로 나눈 결과값을 활용합니다.
- 파라미터로 들어온 데이터의 label을 x축에 텍스트(hour)로 정합니다.
- drawContext.canvas.nativeCanvas.apply { drawText(...) ] 의 의미는 컴포즈는 내부적으로 android.graphics.Canvas를 사용하는데, ‘.nativeCanvas.’ 는 추상화에서 - 한 단계 내려가 안드로이드 원본 Canvas를 사용하게 됩니다. 이 이유는 drawtext 같이 paint를 사용하여 텍스트의 세밀한 설정이 필요한 경우 안드로이드 원본 Canvas가 필요하기 때문입니다.
- drawText 를 통하여  x축 라벨의  x축, y축 좌표와 텍스트 정보를 넘겨줍니다.


- 주황선 : spacing
- 초록선: spacePerHour
- 빨강선 : sizeHeight


3. 

<img width="650" height="300" alt="Image" src="https://github.com/user-attachments/assets/c66a029d-c6a3-4a5d-96a9-870fe3012b7c" />
<img width="200" height="400" alt="Image" src="https://github.com/user-attachments/assets/21fb07be-e148-4c6e-ae0c-79da3e3a322c" /> 

- y축 라벨 그리는 부분에서는 ratio, yPos가 다르게 구현이 되는데, ratio는 최대와 최소의 범위 중 순회하는 데이터가 얼마만큼 차지하느냐를 정하고 있습니다. ( 예를 들면 0 ~ 1.0 까지의 비율로 나타나게 됩니다. ),
- yPos는 왼쪽상단이 0,0이고 오른쪽으로 갈수록 x 축 증가, 아래로 갈수록 y축 증가 라는 것을 이해해야 합니다. 결국 x축 바닥에서부터 차지하는 비율만큼을 뺀 위치에 y좌표 라벨이 설정되게 됩니다.
- 데이터가 lowerValue <= .. <= uppervalue이기 때문에 사이값 모두 y축 라벨을 그리게 됩니다.
- 이하 코드는 위에서 설명한 것과 동일합니다.






4.

<img width="550" height="284" alt="Image" src="https://github.com/user-attachments/assets/1f0fd06c-bc60-40b4-95e6-60c35227de81" />
<img width="200" height="400" alt="Image" src="https://github.com/user-attachments/assets/de973e6c-fe45-4641-a45c-678a8bbadfe1" /> 

- 그리드 선이라는 것은 line chart에 그려지는 보조선을 의미합니다.
- 먼저 가로 그리드선은 spacing 변수는 가로축 세로축의 여유 공간으로, 제외하고 설명을 하면 , 세로의 해당하는 높이에서 데이터의 수치가 차지하는 세로의 비율만큼을 빼면 그것이 데이터의 가로 위치가 됩니다.
- 여기서 데이터는 lowerValue 부터 upperValue까지 이므로 사이값 모두 가로의 모든 선에 그리드선을 그리게 됩니다.
- drawLine 이라는 함수를 통해서 그리드 선의 색상, 선의 시작과 끝 좌표, 선의 굵기를 정하여 실제 그리게 됩니다.


5.

<img width="550" height="257" alt="Image" src="https://github.com/user-attachments/assets/7555577a-2899-4fe5-8ded-cebb673a22b1" /> 
<img width="200" height="400" alt="Image" src="https://github.com/user-attachments/assets/9e4cab5e-2be0-4cdd-83d8-843df07e53e6" />

- 데이터를 순환하면서 순서와 간격을 곱한 만큼의 x좌표를 갖게 됩니다. 
- 즉 (x*spacePerHour, ,0) 에서 (x*spacePerHour, height-spacing)까지 선을 그리게 됩니다.

6.

<img width="550" height="547" alt="Image" src="https://github.com/user-attachments/assets/3e117173-e14a-4ece-990f-557b715b9c91" />
<img width="200" height="400" alt="Image" src="https://github.com/user-attachments/assets/9e49f0c2-1185-41fb-a8b1-961ecc269b1a" /> 

- 이제 x축, y축, 그리드 선을 모두 그렸으니 데이터를 연결합니다.
- 해당 사진 코드에서는 직접 연결하는 것이 아니라  선을 연결할 데이터를 표시해두는 과정입니다. 
- 그것을 Path라고도 합니다. 모든 데이터를 순회하며 각 x,y 좌표를 x1,y1 변수에 저장합니다.
- 처음 데이터는 moveTo(x1,y1) 함수를 통하여 연결될 선의 시작점을 기억합니다.
- 그리고 lineTo()함수를 통해 연결될 선의 위치를 기억하고 있습니다.
- 그러므로 선의 시작점과 연결될 좌표에 대한 정보는 모두 Path의 속성으로 저장 됩니다.
- 위에서 정보가 담긴 Strokepath를 drawPath 함수의 매개변수로 넘겨주고, 선의 색상과 스타일을 정의해주면 실제 선이 그래프에 그려집니다.

7.


<img width="577" height="444" alt="Image" src="https://github.com/user-attachments/assets/e13cb6fd-b5ab-4c7a-9254-6d4e08ec7d98" />
<img width="150" height="300" alt="Image" src="https://github.com/user-attachments/assets/47d122f3-ecb7-4bf7-acd7-83dcc86be0b1" />
<img width="150" height="300" alt="Image" src="https://github.com/user-attachments/assets/d4e6c887-c739-4249-8cf1-ccee3c7ebae1" />

- 이제 색칠할 구역을 정의합니다,fillPath는 strokPath에 데이터를 추가하여 닫힌 도형을 만듭니다.
- 2번째 줄 lineTo() 함수를 통해 그래프 끝에서 밑으로 내려온 곳을 저장하고
- 3번째 줄 lineTo()에서는 그래프의 시작점 맨 아래를 저장하게 됩니다.
- close() 함수는 path를 시작점으로 연결하여 닫힌 도형을 만들게 됩니다. 최종적으로 fillPath라는 이름의 Path 타입 변수를 갖게 됩니다.


- 위의 path 타입의 fillPath가 drawPath() 함수에 전달 됩니다.
- drawPath는 style매개 변수에 기본으로 Fill이 설정되어 있기 때문에 여기서는 style 매개변수에 설정값을 주지 않았습니다.
- 하지만 style 매개변수에 stroke()를 주게 되면 내부가 채워지는 것이 아니라 겉에 테두리를 칠하게 됩니다. 
- 그러므로 drawPath는 style에 어떤 변수를 할당하느냐에 따라 다르게 사용됩니다.
- brush는 채울 방식을 정의하는데, verticalGradient로 세로 방향 그라데이션을 의미하고, transparentGraphColor는 위에서 정의한 그라데이션 상위의 색상이고,
- Color.transparent는 투명한 색이 그라데이션 하위에 올 것을 의미합니다.
- 그라데이션이 끝나는 좌표 endY도 정의합니다. 즉 x축이 있는 곳 까지 그라데이션이 입혀짐을 의미합니다.


- 빨간선 - lineTo(size.width - spacePerHour, size.height - spacing)
- 파랑선 - lineTo(spacing, size.height - spacing)
- 초록선 - close()

## Source Code
- You can check source code [here](https://github.com/0508gyhun/Custom-LineChart_android)


