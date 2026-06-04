package com.ghostfire.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Redis 位图布隆过滤器 — 双哈希法生成 k 个 bit 位
 */
@Component
@RequiredArgsConstructor
public class BloomFilterHelper {

    private final StringRedisTemplate redisTemplate;

    /** 位图大小，2^23 ≈ 838 万位，误判率 < 1% */
    private static final long SIZE = 1L << 23;

    /** 哈希函数个数 */
    private static final int HASH_COUNT = 7;

    public void add(String key, String value) {
        long[] offsets = getOffsets(value);
        for (long offset : offsets) {
            redisTemplate.opsForValue().setBit(key, offset, true);
        }
    }

    public boolean mightContain(String key, String value) {
        long[] offsets = getOffsets(value);
        for (long offset : offsets) {
            Boolean bit = redisTemplate.opsForValue().getBit(key, offset);
            if (bit == null || !bit) {
                return false;
            }
        }
        return true;
    }

    /** FNV + Murmur 双哈希法 */
    private long[] getOffsets(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        long hash1 = fnvHash(bytes);
        long hash2 = murmurHash(bytes);
        long[] offsets = new long[HASH_COUNT];
        for (int i = 0; i < HASH_COUNT; i++) {
            offsets[i] = Math.abs((hash1 + (long) i * hash2) % SIZE);
        }
        return offsets;
    }

    private long fnvHash(byte[] data) {
        long hash = 0xcbf29ce484222325L;
        for (byte b : data) {
            hash ^= b;
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    private long murmurHash(byte[] data) {
        long seed = 0xe17a1465L;
        int len = data.length;
        long h = seed;
        for (int i = 0; i + 7 < len; i += 8) {
            long k = ((long) data[i] & 0xff)
                    | (((long) data[i + 1] & 0xff) << 8)
                    | (((long) data[i + 2] & 0xff) << 16)
                    | (((long) data[i + 3] & 0xff) << 24)
                    | (((long) data[i + 4] & 0xff) << 32)
                    | (((long) data[i + 5] & 0xff) << 40)
                    | (((long) data[i + 6] & 0xff) << 48)
                    | (((long) data[i + 7] & 0xff) << 56);
            k = mixMurmurBlock(k);
            h ^= k;
            h = Long.rotateLeft(h, 27);
            h = h * 5 + 0x52dce729L;
        }
        int tail = len - (len % 8);
        long k = 0;
        for (int i = len - 1; i >= tail; i--) {
            k <<= 8;
            k |= (data[i] & 0xff);
        }
        if (k != 0) {
            k = mixMurmurBlock(k);
            h ^= k;
        }
        h ^= len;
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return h;
    }

    private long mixMurmurBlock(long k) {
        k *= 0x87c37b91114253d5L;
        k = Long.rotateLeft(k, 31);
        k *= 0x4cf5ad432745937fL;
        return k;
    }
}
