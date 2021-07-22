let main = {
    init: () => {
        $('#btn-save').on('click', () => {
            this.save();
        });
    },
    save: () => {
        let data = {
            productId: Number($('productId').val()),
            name: $('name').val(),
            weight: Number($('weight').val())
        };

        $.ajax({
            type: 'POST',
            url: '',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(() => {
            alert('글이 등록되었습니다.');
            window.location.href = '/';
        }).fail((error) => {
            alert(JSON.stringify(error));
        })
    }
};

main.init();