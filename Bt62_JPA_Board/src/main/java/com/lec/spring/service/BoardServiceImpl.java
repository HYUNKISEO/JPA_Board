package com.lec.spring.service;

import com.lec.spring.domain.Attachment;
import com.lec.spring.domain.Post;
import com.lec.spring.domain.User;
import com.lec.spring.repository.AttachmentRepository;
import com.lec.spring.repository.PostRepository;
import com.lec.spring.repository.UserRepository;
import com.lec.spring.util.U;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@Service
public class BoardServiceImpl implements BoardService {

    @Value("${app.pagination.page_rows}")
    private int PAGE_ROWS;

    @Value("${app.pagination.write_pages}")
    private int WRITE_PAGES;

    @Value("${app.upload.path}")
    private String uploadDir;

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AttachmentRepository attachmentRepository;

    // 글 작성
    @Override
    public int write(Post post, Map<String, MultipartFile> files) {
        // 현재 로그인한 작성자 정보.
        User user = U.getLoggedUser();

        // 위 정보는 session 의 정보이고, 일단 DB 에서 다시 읽어온다
        user = userRepository.findById(user.getId()).orElse(null);
        post.setUser(user);  // 글 작성자 세팅
        post.setViewCnt(0L);

        int cnt  = 0;
                Post save = postRepository.save(post);
                if (save != null) {
                    cnt++;
                }

        // 첨부파일 추가
        addFiles(files, post.getId());

        return cnt;
    }

    // 특정 글(id) 첨부파일(들) 추가
    private void addFiles(Map<String, MultipartFile> files, Long id) {
        if(files != null){
            for(var e : files.entrySet()){
                // name="upfile##" 인 경우만 첨부파일 등록. (이유, 다른 웹에디터와 섞이지 않도록..ex: summernote)
                if(!e.getKey().startsWith("upfile")) continue;

                // 첨부 파일 정보 출력
                System.out.println("\n첨부파일 정보: " + e.getKey());   // name 값
                U.printFileInfo(e.getValue());  // 파일 정보 출력
                System.out.println();

                // 물리적인 파일저장
                Attachment file = upload(e.getValue());

                // 성공하면 DB 에도 저장
                if(file != null){
                    file.setPost(postRepository.findById(id).orElse(null));
                    attachmentRepository.save(file);
                }
            }
        }
    } // end addFiles()

    // 물리적으로 파일 저장. 중복된 이름 rename 처리
    private Attachment upload(MultipartFile multipartFile) {
        Attachment attachment = null;

        // 담긴 파일이 없으면 pass
        String originalFilename = multipartFile.getOriginalFilename();
        if(originalFilename == null || originalFilename.length() == 0) return null;

        // 원본 파일명
        String sourcename = StringUtils.cleanPath(multipartFile.getOriginalFilename());

        // 저장될 파일명
        String fileName = sourcename;

        // 중복 파일명 확인
        File file = new File(uploadDir, sourcename);
        if(file.exists()){  // 이미 존재하는 파일명, 중복되면 다른 이름으로 변경하여 저장
            // a.txt => a_2378142783946.txt  : time stamp 값을 활용할거다!
            int pos = fileName.lastIndexOf(".");
            if(pos > -1){  //확장자가 있는 경우
                String name = fileName.substring(0, pos);  // 파일 '이름'
                String ext = fileName.substring(pos + 1);  // 파일 '확장자'

                // 중복방지를 위한 새로운 이름 (현재시간 ms) 를 파일명에 추가
                fileName = name + "_" + System.currentTimeMillis() + "." + ext;
            } else {  // 확장자가 없는 경우
                fileName += "_" + System.currentTimeMillis();
            }
        }
        // 저장할 파일명
        System.out.println("fileName: " + fileName);

        // java.nio
        Path copyOfLocation = Paths.get(new File(uploadDir, fileName).getAbsolutePath());
        System.out.println(copyOfLocation);

        try {
            // inputStream을 가져와서
            // copyOfLocation (저장위치)로 파일을 쓴다.
            // copy의 옵션은 기존에 존재하면 REPLACE(대체한다), 오버라이딩 한다
            Files.copy(
                    multipartFile.getInputStream(),
                    copyOfLocation,
                    StandardCopyOption.REPLACE_EXISTING   // 기존에 존재하면 덮어쓰기
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        attachment = Attachment.builder()
                .filename(fileName)     // 저장된 이름
                .sourcename(sourcename)     // 원본 이름
                .build();

        return attachment;
    }

    // 특정 id 의 글 조회
    // 1. 조회수 증가
    // 2. 글 읽어오기
    @Override
    @Transactional  // 이 메소드는 '트랜잭션' 처리 쿼리문 2개가 같이진행 또는 같이안됨
    public Post detail(Long id) {
        Post post = postRepository.findById(id).orElse(null);

        if(post != null){
            post.setViewCnt(post.getViewCnt() + 1);

            // 첨부파일(들) 정보 가져오기
            List<Attachment> fileList = postRepository.findById(id).get().getFileList();
            setImage(fileList);   // 이미지 파일 여부 세팅
            post.setFileList(fileList);
        }

        return post;
    }

    // 이미지 파일 여부 세팅
    private void setImage(List<Attachment> fileList) {
        // upload 실제 물리적인 경로
        String realPath = new File(uploadDir).getAbsolutePath();

        for(var attachment : fileList){
            BufferedImage imgData = null;
            File f = new File(realPath, attachment.getFilename());  // 저장된 첨부파일에 대한 File 객체

            try {
                imgData = ImageIO.read(f);
            } catch (IOException e) {
                System.out.println("파일존재안함: " + f.getAbsolutePath() + "[" + e.getMessage() + "]");
                throw new RuntimeException(e);
            }

            if(imgData != null) attachment.setImage(true);   // 이미지 여부 체크!

        }
    }


    @Override
    public List<Post> list() {
        return postRepository.findAll(Sort.by(Sort.Order.desc("id")));
    }

    @Override
    public List<Post> list(Integer page, Model model){
        // 현재 페이지 parameter
        if(page == null) page = 1;  // 디폴트는 1page
        if(page < 1) page = 1;

        // 페이징
        // writePages: 한 [페이징] 당 몇개의 페이지가 표시되나
        // pageRows: 한 '페이지'에 몇개의 글을 리스트 할것인가?
        HttpSession session = U.getSession();
        Integer writePages = (Integer)session.getAttribute("writePages");
        if(writePages == null) writePages = WRITE_PAGES;  // 만약 session 에 없으면 기본값으로 동작
        Integer pageRows = (Integer)session.getAttribute("pageRows");
        if(pageRows == null) pageRows = PAGE_ROWS;  // 만약 session 에 없으면 기본값으로 동작

        // 현재 페이지 번호 -> session 에 저장
        session.setAttribute("page", page);

        long cnt = postRepository.count(); // 글목록 전체의 개수
        int totalPage = (int) Math.ceil((double)cnt / pageRows);

        // [페이징] 에 표시할 '시작페이지' 와 '마지막페이지'
        int startPage = 0;
        int endPage = 0;

        //해당 페이지의 글 목록
        List<Post> list = null;

        if(cnt > 0) {  // 데이터가 최소 1개 이상 있는 경우만 페이징
            // page 값 보정
            if(page > totalPage) page = totalPage;

            //몇번째 데이터부터 fromRow
            int fromRow = (page - 1) * pageRows;

            // [페이징] 에 표시할 '시작페이지' 와 '마지막페이지' 계산
            startPage = (((page - 1) / writePages) * writePages) + 1;
            endPage = startPage + writePages - 1;
            if (endPage >= totalPage) endPage = totalPage;

            // 해당 페이지의 글 목록 읽어오기
            Pageable pageable = PageRequest.of(page -1, pageRows, Sort.by(Sort.Order.desc("id")));
            Page<Post> PostPage = postRepository.findAll(pageable);
            list = PostPage.getContent();

            model.addAttribute("list", list);
        } else {
            page = 0;
        }

        model.addAttribute("cnt", cnt);  // 전체 글 개수
        model.addAttribute("page", page); // 현재 페이지
        model.addAttribute("totalPage", totalPage);  // 총 '페이지' 수
        model.addAttribute("pageRows", pageRows);  // 한 '페이지' 에 표시할 글 개수

        // [페이징]
        model.addAttribute("url", U.getRequest().getRequestURI());  // 목록 url
        model.addAttribute("writePages", writePages); // [페이징] 에 표시할 숫자 개수
        model.addAttribute("startPage", startPage);  // [페이징] 에 표시할 시작 페이지
        model.addAttribute("endPage", endPage);   // [페이징] 에 표시할 마지막 페이지

        return list;
    }

    // 특정 id 글 읽어오기
    // 조회수 증가 없음
    @Override
    public Post selectById(Long id) {

        Post post = postRepository.findById(id).orElse(null);

        if(post != null){
            // 첨부파일 정보 가져오기
            List<Attachment> fileList = post.getFileList();
            setImage(fileList);   // 이미지 파일 여부 세팅
            post.setFileList(fileList);
        }

        return post;
    }

    //글 수정
    @Override
    public int update(Post post   // <- id, subject, content
            , Map<String, MultipartFile> files  // 새로 추가된 첨부파일들
            , Long[] delfile) { // 삭제될 첨부파일들
        int result = 0;

        Post p = postRepository.findById(post.getId()).orElse(null);

        if (p != null) {
            post.setViewCnt(p.getViewCnt());
            post.setUser(p.getUser());
            postRepository.save(post);

            // 새로운 첨부파일 추가
            addFiles(files, post.getId());

            // 삭제할 첨부파일(들) 삭제
            if(delfile != null){
                for(Long fileId : delfile){
                    Attachment file = attachmentRepository.findById(fileId).orElse(null);
                    if(file != null){
                        delFile(file);   // 물리적으로 파일 삭제
                        attachmentRepository.delete(file);
                    }
                }
            }
            result =1;
        }

        return result;
    }

    // 특정 첨부파일(id) 를 물리적으로 삭제
    private void delFile(Attachment file) {
        String saveDirectory = new File(uploadDir).getAbsolutePath();
        File f = new File(saveDirectory, file.getFilename());   // 물리적으로 저장된 파일들이 삭제 대상
        System.out.println("삭제시도--> " + f.getAbsolutePath());

        if(f.exists()){
            if(f.delete()){
                System.out.println("삭제 성공");
            } else {
                System.out.println("삭제 실패");
            }
        }else {
            System.out.println("파일이 존재하지 않습니다.");
        }
    }

    @Override
    public int deleteById(Long id) {
        int result = 0;
        Post post = postRepository.findById(id).orElse(null);
        if (post != null) {   // 존재한다면 삭제 진행.
            // 물리적으로 저장된 첨부파일(들) 삭제
            List<Attachment> fileList = post.getFileList();
            if(fileList != null && fileList.size() > 0){
                for(Attachment file : fileList){
                    delFile(file);
                }
            }
            // 글삭제
            postRepository.deleteById(id);
            result = 1;
        }
        return result;
    }
}
