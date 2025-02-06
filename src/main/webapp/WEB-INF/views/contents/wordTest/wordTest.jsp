<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>단어 테스트</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .question-card {
            margin-bottom: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .choices {
            margin-top: 15px;
        }
        .result-feedback {
            display: none;
            margin-top: 10px;
        }
        .submit-section {
            margin: 30px 0;
            text-align: center;
        }
        .score-summary {
            background-color: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
        }
        .results-detail {
            background-color: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .result-item {
            padding: 10px;
            border-bottom: 1px solid #eee;
        }
        .result-item:last-child {
            border-bottom: none;
        }
        .progress {
            height: 25px;
        }
        .progress-bar {
            transition: width 0.6s ease;
        }
    </style>
</head>
<body>
<div class="container mt-4">
    <div id="testContainer">
        <div id="testHeader" class="text-center mb-4">
            <h2>단어 테스트</h2>
            <div class="progress mb-3">
                <div class="progress-bar" role="progressbar" style="width: 0%"></div>
            </div>
        </div>
        <div id="testContent"></div>
        <div class="submit-section">
            <button id="submitTest" class="btn btn-primary">제출하기</button>
        </div>
        <div id="resultSection" class="mt-4" style="display: none;">
            <h3 class="text-center">테스트 결과</h3>
            <div id="scoreDisplay" class="text-center mb-4"></div>
            <div class="results-detail mt-4" id="resultDetails"></div>
        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
    $(document).ready(function() {
        // 세션 스토리지에서 테스트 데이터와 타입 가져오기
        const testData = JSON.parse(sessionStorage.getItem('testData'));
        const testType = sessionStorage.getItem('testType');

        console.log('Raw testData:', sessionStorage.getItem('testData'));
        console.log('Session Storage - Test Data:', testData);
        console.log('Session Storage - Test Type:', testType);

        if (!testData || !testData.questions || !Array.isArray(testData.questions)) {
            console.error('Invalid test data structure:', testData);
            alert('테스트 데이터가 없습니다.');
            window.location.href = '/contents/wordTest';
            return;
        }

        // 문제 데이터 가져오기
        const questions = testData.questions;
        console.log('Questions loaded:', questions);
        const testTypeTitle = testType === 'meaning' ? '영어 → 한글' : '한글 → 영어';
        console.log('Test Type Title:', testTypeTitle);

        $('#testHeader h2').text(testTypeTitle + ' 테스트');

        // 문제 렌더링
        questions.forEach((question, index) => {
            const questionHtml = createQuestionHTML(question, index);
            console.log('Rendering question ' + (index + 1) + ':', question);
            $('#testContent').append(questionHtml);
        });

        $(document).on('change', 'input[type="radio"]', function() {
            const total = questions.length;
            const answered = $('input[type="radio"]:checked').length;
            const percentage = (answered / total * 100).toFixed(1);

            $('.progress-bar')
                .css('width', percentage + '%')
                .text(percentage + '%');
        });

        // 제출 버튼 클릭 이벤트
        $('#submitTest').click(function() {
            console.log('Submit button clicked');

            // 모든 문제 풀었는지 확인
            let unansweredQuestions = [];
            let allAnswered = true;

            questions.forEach((question, index) => {
                const selectedRadio = $('input[name="question' + index + '"]:checked');
                const questionCard = $('#question' + index);
                const feedback = questionCard.find('.result-feedback');


                console.log('Question ' + (index + 1) + ':', {
                    question: question,
                    selectedRadio: selectedRadio.length > 0 ? selectedRadio.val() : 'none',
                    correct_answer: question.correct_answer
                });

                if (!selectedRadio.length) {
                    unansweredQuestions.push(index + 1);
                    allAnswered = false;
                }
            });

            console.log('Unanswered Questions:', unansweredQuestions);
            console.log('All Answered:', allAnswered);

            // 풀지 않은 문제가 있는 경우
            if (!allAnswered) {
                console.log('Showing confirm dialog for unanswered questions:', unansweredQuestions.join(', '));
                const unansweredText = unansweredQuestions.map(num => num + "번").join(', ');
                const confirmSubmit = confirm(
                    "문제 " + unansweredText + "을 풀지 않았습니다.\n정말 제출하시겠습니까?"
                );
                if (!confirmSubmit) {
                    return;
                }
            }


            // 결과 처리 부분
            let score = 0;
            let total = questions.length;
            let resultDetailsHtml = '';

            questions.forEach((question, index) => {
                const selectedRadio = $('input[name="question' + index + '"]:checked');
                const questionCard = $(`#question` + index);
                const feedback = questionCard.find('.result-feedback');

                // 디버깅을 위한 로그 추가
                console.log('Processing Question ' + (index + 1) + ':', {
                    question: question,
                    selectedRadio: selectedRadio.length ? selectedRadio.val() : 'none',
                    correct_answer: question.correct_answer
                });

                // 답을 선택하지 않은 경우
                if (!selectedRadio.length) {
                    feedback.html('<div class="alert alert-warning">답을 선택하지 않았습니다.</div>');
                    feedback.show();

                    resultDetailsHtml += createResultItemHtml({
                        questionNum: index + 1,
                        answered: false,
                        correct: false,
                        selectedAnswer: '미응답',
                        correctAnswer: question.choices[question.correct_answer]
                    });
                } else {
                    // 답을 선택한 경우
                    const selectedAnswer = parseInt(selectedRadio.val());

                    // 디버깅을 위한 로그 추가
                    console.log('Comparing answers for question ' + (index + 1) + ':', {
                        selectedAnswer: selectedAnswer,
                        correct_answer: question.correct_answer,
                        isEqual: selectedAnswer === question.correct_answer
                    });

                    const isCorrect = selectedAnswer === question.correct_answer;

                    if (isCorrect) {
                        score++;
                        feedback.html('<div class="alert alert-success">' +
                            '<strong>정답입니다!</strong><br>' +
                            question.word + ' = ' + question.choices[question.correct_answer] +
                            '</div>');
                    } else {
                        feedback.html('<div class="alert alert-danger">' +
                            '<strong>오답입니다.</strong><br>' +
                            '선택한 답: ' + question.choices[selectedAnswer] + '<br>' +
                            '정답: ' + question.choices[question.correct_answer] +
                            '</div>');
                    }

                    resultDetailsHtml += createResultItemHtml({
                        questionNum: index + 1,
                        answered: true,
                        correct: isCorrect,
                        selectedAnswer: question.choices[selectedAnswer],
                        correctAnswer: question.choices[question.correct_answer]
                    });
                }

                feedback.show();
                questionCard.find('input[type="radio"]').prop('disabled', true);
            });


            // 결과 표시
            const percentage = (score / total * 100).toFixed(1);

            $('#scoreDisplay').html(
                '<div class="score-summary">' +
                '<h4>총점: ' + score + '/' + total + ' (' + percentage + '%)</h4>' +
                '<p>정답 ' + score + '개 / 오답 ' + (total - score) + '개</p>' +
                '</div>'
            );

            $('#resultDetails').html(resultDetailsHtml);
            $('#resultSection').show();
            $('#submitTest').prop('disabled', true);

            // 진행률 바 업데이트
            $('.progress-bar')
                .css('width', percentage + '%')
                .text(percentage + '%')
                .addClass(percentage >= 60 ? 'bg-success' : 'bg-danger');
        });
    });

    // 문제 HTML 생성 함수
    function createQuestionHTML(question, index) {
        const questionNumber = index + 1;

        console.log('Creating question HTML:', {
            questionNumber: questionNumber,
            question: question,
            word: question.word,
            choices: question.choices,
            correct_answer: question.correct_answer
        });

        console.log('Creating question number:', questionNumber);

        const testType = sessionStorage.getItem('testType');
        let questionWord = question.word;
        let choices = question.choices;

        let html = '<div id="question' + index + '" class="card question-card">' +
            '<div class="card-body">' +
            '<h5 class="card-title">문제 ' + questionNumber + '</h5>' +
            '<p class="card-text">' + questionWord + '</p>' +
            '<div class="choices">';

        choices.forEach((choice, i) => {
            html += '<div class="form-check">' +
                '<input class="form-check-input" type="radio" ' +
                'name="question' + index + '" ' +
                'value="' + i + '" ' +
                'id="choice' + index + '_' + i + '">' +
                '<label class="form-check-label" for="choice' + index + '_' + i + '">' +
                choice +
                '</label>' +
                '</div>';
        });

        html += '</div>' +
            '<div class="result-feedback"></div>' +
            '</div>' +
            '</div>';

        return html;
    }

    // 결과 아이템 HTML 생성 함수
    function createResultItemHtml(result) {
        const statusClass = !result.answered ? 'text-warning' :
            (result.correct ? 'text-success' : 'text-danger');
        const statusText = !result.answered ? '미응답' :
            (result.correct ? '정답' : '오답');

        let html = '<div class="result-item mb-2">' +
            '<span class="question-number">문제 ' + result.questionNum + '번:</span>' +
            '<span class="' + statusClass + '"> ' + statusText + '</span>';

        if (result.answered && !result.correct) {
            html += '<br><small class="text-muted">' +
                '선택한 답: ' + result.selectedAnswer + ' / ' +
                '정답: ' + result.correctAnswer +
                '</small>';
        }

        html += '</div>';
        return html;
    }
</script>
</body>
</html>
