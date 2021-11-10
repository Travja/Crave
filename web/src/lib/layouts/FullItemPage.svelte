<script>
    import {formatter} from "$lib/util";

    export let items, item, src;
    let imgUpload;

    const changeImg = e => {
        src = URL.createObjectURL(e.target.files[0]);
    };
</script>

<div class="parent full">
    {#if !items}
        <div>Item not found</div>
    {:else if item}
        <div class="hcenter" id="imgBox">
            <img alt="{item.item.name}" on:click={() => imgUpload.click()} src={item.item?.image ? item.item.image :
            src}/>
            <input accept="image/*" bind:this={imgUpload} id="image" name="image" on:change={changeImg}
                   type="file"/>
            <div class="clear"/>
            <div></div>
        </div>
        <div>
            <h1>{item.item.name}</h1>
            <div class="price">{formatter.format(item.price)}</div>
            <hr/>
            <slot></slot>
        </div>
    {/if}
</div>

<style>
    .parent {
        margin: 10px;
    }

    .price {
        /*float: right;*/
        font-size: 1.1em;
        color: #555;
    }

    img {
        width: 30vw;
        height: 30vw;
        max-width: 300px;
        max-height: 300px;
        object-fit: contain;
        box-sizing: border-box;

        border: 2px solid black;
        box-shadow: 5px 5px 5px #333;
        margin-bottom: 10px;
        padding: 10px;
    }

    h1 {
        font-size: 2.3em;
        margin: 0;
    }

    #imgBox {
        float: left;
        margin-bottom: 10px;
        margin-right: 15px;
    }

    input[type="file"] {
        display: none;
    }
</style>