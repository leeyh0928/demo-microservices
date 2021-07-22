let main = {
    init: () => {
        $('#btn-create').on('click', () => {
            this.create();
        });

        $('#btn-update').on('click', () => {
            this.update();
        });

        $('#btn-delete').on('click', () => {
           this.delete();
        });
    },
    create: () => {
        let data = {
            productId: Number($('#productId').val()),
            name: $('#name').val(),
            weight: Number($('#weight').val())
        };

        $.ajax({
            type: 'POST',
            url: '/api/product',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(() => {
            alert('상품이 등록되었습니다.');
            window.location.href = '/';
        }).fail((error) => {
            alert(JSON.stringify(error));
        })
    },
    update: () => {
        let data = {
            name: $('#name').val(),
            weight: Number($('#weight').val())
        }

        let id = Number($('#productId').val());

        $.ajax({
            type: 'PUT',
            url: '/api/product/' + id,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(() => {
            alert('상품이 수정되었습니다.');
            window.location.href = '/';
        }).fail((error) => {
            alert(JSON.stringify(error));
        })
    },
    delete: () => {
        let id = Number($('#productId').val());

        $.ajax({
            type: 'DELETE',
            url: '/api/product/' + id,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8'
        }).done(() => {
            alert('상품이 삭제되었습니다.');
            window.location.href = '/';
        }).fail((error) => {
            alert(JSON.stringify(error));
        })
    }
};

main.init();